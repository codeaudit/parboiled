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
 * A Matcher that repeatedly tries its sub matcher against the input. Always succeeds.
 *
 * @param <V>
 */
public class ZeroOrMoreMatcher<V> extends AbstractMatcher<V> implements FollowMatcher<V> {

    public final Matcher<V> subMatcher;

    public ZeroOrMoreMatcher(@NotNull Rule subRule) {
        super(subRule);
        this.subMatcher = getChildren().get(0);
    }

    public boolean match(@NotNull MatcherContext<V> context) {
        context.clearEnforcement();
        
        InputLocation lastLocation = context.getCurrentLocation();
        while (context.getSubContext(subMatcher).runMatcher()) {
            InputLocation currentLocation = context.getCurrentLocation();
            if (currentLocation == lastLocation) {
                Checks.fail("The inner rule of ZeroOrMore rule '%s' must not allow empty matches", context.getPath());
            }
            lastLocation = currentLocation;
        }

        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        Characters chars = subMatcher.getStarterChars();
        Checks.ensure(!chars.contains(Chars.EMPTY),
                "Rule '%s' must not allow empty matches as sub-rule of a ZeroOrMore-rule", subMatcher);
        return chars;
    }

    public Characters getFollowerChars(MatcherContext<V> context) {
        return getStarterChars().add(Chars.EMPTY);
    }

    public void accept(@NotNull MatcherVisitor<V> visitor) {
        visitor.visit(this);
    }

}