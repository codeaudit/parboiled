/*
 * Copyright (C) 2009 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.matchers;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;
import org.parboiled.support.Checks;
import org.parboiled.support.InputLocation;

/**
 * A special Matcher not actually matching any input but rather trying its sub matcher against the current input
 * position. Succeeds if the sub matcher would succeed (not inverted) or fail (inverted).
 *
 * @param <V>
 */
public class TestMatcher<V> extends AbstractMatcher<V> {

    private final boolean inverted;
    private final Matcher<V> subMatcher;

    public TestMatcher(@NotNull Rule subRule, boolean inverted) {
        super(subRule);
        this.subMatcher = getChildren().get(0);
        this.inverted = inverted;
    }

    @Override
    public String getLabel() {
        return hasLabel() ? super.getLabel() : (inverted ? "!(" : "&(") + getChildren().get(0) + ")";
    }

    public boolean match(@NotNull MatcherContext<V> context) {
        InputLocation lastLocation = context.getCurrentLocation();
        boolean matched = context.getSubContext(subMatcher).runMatcher();
        if (matched && context.getCurrentLocation() == lastLocation && lastLocation.currentChar != Chars.EOI) {
            Checks.fail("The inner rule of Test/TestNot rule '%s' must not allow empty matches", context.getPath());
        }
        context.setCurrentLocation(lastLocation); // reset location, test matchers never advance

        return inverted ? !matched : matched;
    }

    public Characters getStarterChars() {
        Characters characters = subMatcher.getStarterChars();
        Checks.ensure(!characters.contains(Chars.EMPTY), "Rule '%s' allows empty matches, " +
                "unlikely to be correct as a sub rule of a Test/TestNot-Rule", subMatcher);
        return inverted ? Characters.ALL_EXCEPT_EMPTY.remove(characters) : characters;
    }

    @Override
    public String getExpectedString() {
        String label = super.getExpectedString();
        if (!(inverted ? "testNot" : "test").equals(label)) return label;
        return (inverted ? "not " : "") + subMatcher.getExpectedString();
    }

}