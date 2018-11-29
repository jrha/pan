#!/usr/bin/env python
# coding=utf8
#
# Copyright 2016 Science & Technology Facilities Council
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import glob
import unittest
from sys import argv
from os.path import basename, dirname, join

import panlint

class TestPanlint(unittest.TestCase):
    def setUp(self):
        self.maxDiff = None
        self.longMessage = True

    def _assert_lint_line(self, input_line, input_diagnoses, input_messages, input_problems, input_first_line=False):
        input_diagnoses.sort()

        result_line, result_first_line = panlint.lint_line(input_line, [], input_first_line)
        self.assertEqual(len(result_line.problems), input_problems)

        result_diagnoses = [p.diagnose() for p in result_line.problems]
        result_diagnoses.sort()

        for d1, d2 in zip(input_diagnoses, result_diagnoses):
            self.assertEqual(d1, d2)

        # If messages is set to None, ignore the contents and just check that is not an empty set
        if input_messages is None:
            for p in result_line.problems:
                self.assertNotEqual(p.message, '')
        else:
            input_messages.sort()
            result_messages = [p.message.text for p in result_line.problems]
            result_messages.sort()
            for m1, m2 in zip(input_messages, result_messages):
                self.assertEqual(m1, m2)

        # first_line must ALWAYS be False when returned
        self.assertEqual(result_first_line, False)

    def _assert_problem_details(self, problem, start, end, msg_id):
        self.assertEqual(problem.start, start)
        self.assertEqual(problem.end, end)
        self.assertEqual(problem.message.id, msg_id)

    def test_diagnose(self):
        dummy_message = panlint.Message('', 0, '')
        self.assertEqual(panlint.Problem(0, 0, dummy_message).diagnose(), '')
        self.assertEqual(panlint.Problem(0, 4, dummy_message).diagnose(), '^^^^')
        self.assertEqual(panlint.Problem(2, 8, dummy_message).diagnose(), '  ^^^^^^')
        self.assertEqual(panlint.Problem(7, 7, dummy_message).diagnose(), '       ')
        self.assertEqual(panlint.Problem(3, -2, dummy_message).diagnose(), '   ')

    def test_print_diagnosis(self):
        FORMAT = '\x1b[34m%s\x1b[39m'
        self.assertEqual(panlint.print_diagnosis(''), FORMAT % '')
        self.assertEqual(panlint.print_diagnosis('so many words'), FORMAT % 'so many words')

    def test_get_string_ranges(self):
        self.assertEqual(
            panlint.get_string_ranges(panlint.Line('', 1, '''there is a "string" in here''')),
            [(11, 19)],
        )
        self.assertEqual(
            panlint.get_string_ranges(panlint.Line('', 1, '''"string" + 'string' + something''')),
            [(0, 8), (11, 19)],
        )

    def test_files(self):
        """
        Test all files in test_files that start with test_*.pan using lint_file
        """
        no_errors = []
        dir_base = join(dirname(argv[0]), 'test_files')
        for afn in glob.glob(join(dir_base, 'test_*.pan')):
            fn = basename(afn)
            if fn.startswith('test_good'):
                self.assertEqual(panlint.lint_file(afn), no_errors)
            else:
                self.assertTrue(False, 'test_files: unknown testfile ' + afn)

    def test_mvn_templates(self):
        dir_base = join(dirname(argv[0]), 'test_files')
        self.assertEqual(len(panlint.lint_file(join(dir_base, 'mvn_template_first_line.pan'), True)), 0)
        self.assertEqual(len(panlint.lint_file(join(dir_base, 'mvn_template_first_line.pan'), False)), 1)

    def test_strip_trailing_comments(self):
        comment_plain = panlint.Line('', 1, '''Words; # This is a trailing comment''')
        comment_in_string = panlint.Line('', 2, '''words = '# Not a trailing comment' + pictures;''')
        comment_mixed = panlint.Line('', 3, '''words = '# Not a trailing comment';#But this is''')

        annotation_plain = panlint.Line('', 4, '''Words; @{This is a trailing annotation}''')
        annotation_in_string = panlint.Line('', 5, '''words = '@{Not a trailing annotation}' + pictures;''')
        annotation_mixed = panlint.Line('', 6, '''words = '@{Not a trailing annotation}';@{But this is}''')

        self.assertEqual(
            panlint.strip_trailing_comments(comment_plain, []).text,
            'Words;'
        )
        self.assertEqual(
            panlint.strip_trailing_comments(comment_in_string, panlint.get_string_ranges(comment_in_string)).text,
            comment_in_string.text
        )
        self.assertEqual(
            panlint.strip_trailing_comments(comment_mixed, panlint.get_string_ranges(comment_mixed)).text,
            '''words = '# Not a trailing comment';'''
        )
        self.assertEqual(
            panlint.strip_trailing_comments(annotation_plain, []).text,
            'Words;'
        )
        self.assertEqual(
            panlint.strip_trailing_comments(annotation_in_string, panlint.get_string_ranges(annotation_in_string)).text,
            annotation_in_string.text
        )
        self.assertEqual(
            panlint.strip_trailing_comments(annotation_mixed, panlint.get_string_ranges(annotation_mixed)).text,
            '''words = '@{Not a trailing annotation}';'''
        )

    def test_whitespace_around_operators(self):
        good = {
            'simple': 'variable ALPHA = 5 + 3;',
            'fn': 'variable ALPHA = afunction() + 3;',
            'fn2': 'variable ALPHA = afunction() + 31;',
            'for': 'for (idx = 31; idx >= 0; idx = idx - 1) {',
            'square_brackets': 'variable EXAMPLE = b[c-1];',
            'negative':  'variable EXAMPLE = -1;',
            # lines that start or end with an operator (i.e. are part of a multi-line expression) should be allowed
            'line_cont': '+ 42;',
            'line_to_be_cont': 'variable EXAMPLE = 42 +',
        }

        bad_tests = {
            'before': (
                panlint.Line('', 2048, 'variable BOBBY = 8* 1;'),
                'Missing space before operator',
                '                 ^^',
            ),
            'after': (
                panlint.Line('', 3072, 'variable BRILLIANT = 16 /2;'),
                'Missing space after operator',
                '                        ^^',
            ),
            'both': (
                panlint.Line('', 4096, 'variable DAVID = 10-2;'),
                'Missing space before and after operator',
                '                  ^^^',
            ),
            'square_brackets': (
                panlint.Line('', 6144, 'variable XAVIER = b[c + 1];'),
                'Unwanted space in simple expression in square brackets',
                '                     ^',
            ),
            'negative': (
                panlint.Line('', 8192, 'variable XANDER = - 1;'),
                'Unwanted space after minus sign (not operator)',
                '                  ^^^',
            ),
        }

        lc = panlint.LineChecks()

        for i, (name, text) in enumerate(good.items()):
            line = panlint.Line('', i, text)
            line = lc.whitespace_around_operators(line, [])
            problems = line.problems
            self.assertEqual(len(problems), 0, name)

        for name, (bad_test, bad_message, bad_diag) in bad_tests.iteritems():
            line = lc.whitespace_around_operators(bad_test, [])
            problems = line.problems
            self.assertEqual(len(problems), 1, name)
            self.assertEqual(problems[0].message.text, bad_message, name)
            self.assertEqual(problems[0].diagnose(), bad_diag, name)

        for s in ['+','-','*','/','>','<','>=','<=']:
            t = 'variable g = 3 %s 4' % s
            print t
            self.assertEqual(lc.whitespace_around_operators(t, []), (True, '', ''))

        good_cond = 'variable A ?= -4;'
        self.assertEqual(lc.whitespace_around_operators(good_cond, []), (True, '', ''))

        good_negative_1 = 'variable l = list(-6, 5, -12);'
        good_negative_2 = 'variable n = 2 * -3;'
        good_negative_3 = 'variable n = -2 * 3;'

        bad_before = 'variable b = 8* 1;'
        dgn_before = '             ^^'

        bad_after = 'variable b = 16 /2;'
        dgn_after = '                ^^'

        bad_both = 'variable d = 10-2;'
        dgn_both = '              ^^^'

        self.assertEqual(lc.whitespace_around_operators(good_negative_1, []), (True, '', ''))
        self.assertEqual(lc.whitespace_around_operators(good_negative_2, []), (True, '', ''))
        self.assertEqual(lc.whitespace_around_operators(good_negative_3, []), (True, '', ''))

        self.assertEqual(lc.whitespace_around_operators(bad_before, []), (False, dgn_before, 'Missing space before operator'))
        self.assertEqual(lc.whitespace_around_operators(bad_after, []), (False, dgn_after, 'Missing space after operator'))
        self.assertEqual(lc.whitespace_around_operators(bad_both, []), (False, dgn_both, 'Missing space before and after operator'))


    def test_whitespace_after_semicolons(self):
        self._assert_lint_line(
            panlint.Line('', 1, 'foreach(k; v;  things) {'),
            ['             ^^'],
            ['Semicolons should be followed exactly one space or end-of-line'],
            1,
        )
        self._assert_lint_line(
            panlint.Line('', 2, 'foreach(k;    v;  things) {'),
            ['          ^^^^', '                ^^'],
            ['Semicolons should be followed exactly one space or end-of-line'],
            2,
        )

    def test_profilepath_trailing_slash(self):
        # Trailing slashes in profile paths
        self._assert_lint_line(
            panlint.Line('', 148, '"/system/hostname" = "foo.example.org";'),
            [],
            [],
            0,
        )
        self._assert_lint_line(
            panlint.Line('', 151, "prefix '/system/network/interfaces/eth0';"),
            [],
            [],
            0,
        )

        self._assert_lint_line(
            panlint.Line('', 795, "'/' = dict();"),
            [],
            [],
            0,
        )

        self._assert_lint_line(
            panlint.Line('', 22, '"/system/hostname/" = "bar.example.org";'),
            ['                 ^'],
            ['Unnecessary trailing slash at end of profile path'],
            1,
        )

        self._assert_lint_line(
            panlint.Line('', 77, '"/system/hostname////////" = "bob.example.org";'),
            ['                 ^^^^^^^^'],
            ['Unnecessary trailing slash at end of profile path'],
            1,
        )

        self._assert_lint_line(
            panlint.Line('', 182, "prefix '/system/aii/osinstall/ks/';"),
            ['                                ^'],
            ['Unnecessary trailing slash at end of profile path'],
            1,
        )

    def test_lint_line(self):
        good_first = panlint.Line('', 120, 'structure template foo.bar;')
        bad_first = panlint.Line('', 122, 'variable foo = "bar";')

        # Test first line checking
        self._assert_lint_line(good_first, [], [], 0, True)

        self._assert_lint_line(bad_first, ['^'*len(bad_first.text)], None, 1, True)

        # Test component inclusion check
        self._assert_lint_line(
            panlint.Line('', 123, '"/software/components/foo/bar" = 42;'),
            ['                      ^^^'],
            None,
            1,
        )

        # Test pattern based checking
        self._assert_lint_line(
            panlint.Line('', 124, '   x = x + 1; # Bad Indentation'),
            ['^^^'],
            None,
            1,
        )

        # Test method based checking
        self._assert_lint_line(
            panlint.Line('', 125, 'x = x+1; # Missing space'),
            ['    ^^^'],
            None,
            1,
        )

        # Test that all three check types co-exist
        self._assert_lint_line(
            panlint.Line('', 126, '  "/software/components/foo/bar" = 42+7;'),
            ['^^', '                        ^^^', '                                    ^^^'],
            None,
            3,
        )

    def test_find_annotation_blocks(self):
        test_text = '''structure template awesome;
        @{ desc = what is the point of this template? }

        'foo' : string
        'bar' ? long

        @{ This stuff on line seven is not code, things like x=x+1 should be ignored here... }
        'simon' : string = 'says';
        '''

        self.assertItemsEqual(panlint.find_annotation_blocks(test_text), [2, 7])
        self.assertEqual(panlint.find_annotation_blocks('template garbage;\n\n# Nothing to see here.\n\n'), [])

    def test_find_heredoc_blocks(self):
        test_text = '''unique template awesome;
        "/something" = 1;
        "/a/b/c" = <<EOFF;
        "/a/" = 1+1;
        EOFF
        "/very" = 1;
        "/more" = <<EOFF;
        hello
        EOFF
        '''

        self.assertItemsEqual(panlint.find_heredoc_blocks(test_text), [4, 5, 8, 9])
        self.assertEqual(panlint.find_heredoc_blocks('template garbage;\n\n# Nothing to see here.\n\n'), [])

    def test_component_use(self):
        # Test a line containing a standard path assignment
        line_standard = panlint.Line('', 100, "'/software/components/chkconfig/service/rdma' = dict(")
        line_standard_commented = panlint.Line('', 101, '# ' + line_standard.text)

        # Test a line setting a path prefix
        line_prefix = panlint.Line('', 200, "prefix '/software/components/metaconfig/services/{/etc/sysconfig/fetch-crl}';")
        line_prefix_commented = panlint.Line('', 201, '# ' + line_prefix.text)

        # Test both lines with components listed as included
        self.assertEqual(
            panlint.lint_line(line_standard, ['chkconfig'], False)[0].problems,
            [],
        )
        self.assertEqual(
            panlint.lint_line(line_prefix, ['metaconfig'], False)[0].problems,
            [],
        )

        # Test both lines without components listed as included
        self._assert_problem_details(panlint.lint_line(line_standard, [], False)[0].problems[0], 22, 31, 'CU001')
        self._assert_problem_details(panlint.lint_line(line_prefix, [], False)[0].problems[0], 29, 39, 'CU001')

        # Test both lines without components listed as included but commented out
        self.assertEqual(len(panlint.lint_line(line_standard_commented, [], False)[0].problems), 0)
        self.assertEqual(len(panlint.lint_line(line_prefix_commented, [], False)[0].problems), 0)


    def test_component_finders(self):
        #Both functions should return lists of component names that can be combined into a single list

        fake_template = "\n".join([
            "object template foo.bar.example.org",
            "",
            "# include 'components/filecopy/config';",
            "include 'components/metaconfig/config';",
            "",
            "prefix '/software/components/filecopy/config';",
        ])

        self.assertEqual(
            panlint.get_components_included(''),
            [],
        )

        self.assertEqual(
            panlint.get_components_included(fake_template),
            ['metaconfig'],
        )

        self.assertEqual(
            panlint.get_unit_test_resource_name('./foo-bar/resources/config.pan'),
            [],
        )

        self.assertEqual(
            panlint.get_unit_test_resource_name('./ncm-fstab/src/test/resources/fstab.pan'),
            ['fstab'],
        )



    def test_check_line_patterns(self):
        lines = [
            ('variable UNIVERSAL_TRUTH = 42;', []),
            ('variable BAD = -1;', ['LP011']),
            ('variable bad_long = ":-(";', ['LP010']),
            ('variable bad = "all lower";', ['LP010', 'LP011']),
            ('variable tricky_onE = "Uhoh";', ['LP010']),
            ('variable camelCase = "camels!";', ['LP010']),
            ('variable TitleCase ?= -3;', ['LP010']),
            ('variable NoSpacesHere?=True;', ['LP010']),
        ]

        for text, ids in lines:
            ids.sort()
            line = panlint.Line('patterns.pan', 0, text)
            problems = panlint.check_line_patterns(line, [])
            problems = [problem.message.id for problem in problems]
            problems.sort()
            self.assertEqual(problems, ids)


if __name__ == '__main__':
    unittest.main()
