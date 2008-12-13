#
# make sure protected resources when optimizing don't
# get changed from one profile to another
#
# @expect="/profile/result='true'"
#

object template protected4;

include { 'set-variable-x' };

'/X' = X;
'/otherX' = value('other:/X');

'/result' = (value('/X/a') == value('/otherX/a')) 
            && (value('/X/b') == value('/otherX/b'));
