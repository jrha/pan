/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/output/PanFormatter.java $
 $Id: PanFormatter.java 3597 2008-08-17 09:08:57Z loomis $
 */

package org.quattor.pan.output;

import java.io.PrintWriter;

import org.quattor.pan.repository.SourceFile;
import org.quattor.pan.tasks.FinalResult;

public class DepFormatter extends AbstractFormatter {

	private static final DepFormatter instance = new DepFormatter();

	private DepFormatter() {
		super("dep", "dep");
	}

	protected DepFormatter(String suffix, String key) {
		super(suffix, key);
	}

	public static DepFormatter getInstance() {
		return instance;
	}

	protected void write(FinalResult result, PrintWriter ps) throws Exception {

		for (SourceFile s : result.getDependencies()) {
			ps.println(s.toString());
		}

	}

}
