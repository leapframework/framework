/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package leap.core.web.path;

import java.util.Comparator;

import leap.lang.annotation.Internal;

//This class is a modified copy from <a href="http://jersey.java.net">jersey</a>.

/**
 * A path pattern that is a regular expression generated from a URI path
 * template.
 * <p>
 * The path pattern is normalized by removing a terminating "/" if present.
 * <p>
 * The path pattern is postfixed with a right hand pattern that consists of either
 * a matching group that matches zero or more path segments,
 * see {@link RightHandPath#capturingZeroOrMoreSegments}, or zero path
 * segments, see {@link RightHandPath#capturingZeroSegments}.
 *
 * @author Paul Sandoz
 */
@Internal
public final class JerseyPathPattern extends JerseyPatternWithGroups {

    /**
     * Empty path pattern matching only empty string.
     */
    public static final JerseyPathPattern EMPTY_PATTERN = new JerseyPathPattern();
    /**
     * Path pattern matching the end of a URI path. Can be either empty {@code ""}
     * or contain a trailing slash {@code "/"}.
     */
    public  static final JerseyPathPattern END_OF_PATH_PATTERN = new JerseyPathPattern("", JerseyPathPattern.RightHandPath.capturingZeroSegments);
    /**
     * Path pattern matching the any URI path.
     */
    public  static final JerseyPathPattern OPEN_ROOT_PATH_PATTERN = new JerseyPathPattern("", RightHandPath.capturingZeroOrMoreSegments);
    /**
     * Path pattern comparator that defers to {@link JerseyUriTemplate#COMPARATOR comparing
     * the templates} associated with the patterns.
     */
    public static final Comparator<JerseyPathPattern> COMPARATOR = new Comparator<JerseyPathPattern>() {

        @Override
        public int compare(JerseyPathPattern o1, JerseyPathPattern o2) {
            return JerseyUriTemplate.COMPARATOR.compare(o1.template, o2.template);
        }
    };

    /**
     * The set of right hand path patterns that may be appended to a path
     * pattern.
     */
    public static enum RightHandPath {

        /**
         * A capturing group that matches zero or more path segments and
         * keeps the matching path template open.
         */
        capturingZeroOrMoreSegments("(/.*)?"),
        /**
         * A capturing group that matches zero segments and effectively
         * closes the matching path template.
         */
        capturingZeroSegments("(/)?");
        //
        private final String regex;

        private RightHandPath(String regex) {
            this.regex = regex;
        }

        private String getRegex() {
            return regex;
        }
    }

    /**
     * Return a new path pattern with a same path template but
     * a {@link RightHandPath#capturingZeroSegments closed} right hand path.
     *
     * @param pattern an (open) path pattern to convert to a closed pattern.
     * @return closed path pattern for the same path template.
     */
    public static JerseyPathPattern asClosed(JerseyPathPattern pattern) {
        return new JerseyPathPattern(pattern.getTemplate().getTemplate(), RightHandPath.capturingZeroSegments);
    }
    //
    private final JerseyUriTemplate template;

    private JerseyPathPattern() {
        super();
        this.template = JerseyUriTemplate.EMPTY;
    }

    /**
     * Create a path pattern and post fix with
     * {@link RightHandPath#capturingZeroOrMoreSegments}.
     *
     * @param template the path template.
     *
     * @see #PathPattern(String, JerseyPathPattern.RightHandPath)
     */
    public JerseyPathPattern(String template) {
        this(new JerseyPathTemplate(template));
    }

    /**
     * Create a path pattern and post fix with
     * {@link RightHandPath#capturingZeroOrMoreSegments}.
     *
     * @param template the path template
     *
     * @see #PathPattern(JerseyPathTemplate, JerseyPathPattern.RightHandPath)
     */
    public JerseyPathPattern(JerseyPathTemplate template) {
        super(postfixWithCapturingGroup(template.getJerseyPattern().getRegex()),
                addIndexForRightHandPathCapturingGroup(template.getJerseyPattern().getGroupIndexes()));

        this.template = template;
    }

    /**
     * Create a path pattern and post fix with a right hand path pattern.
     *
     * @param template the path template.
     * @param rhpp the right hand path pattern postfix.
     */
    public JerseyPathPattern(String template, RightHandPath rhpp) {
        this(new JerseyPathTemplate(template), rhpp);
    }

    /**
     * Create a path pattern and post fix with a right hand path pattern.
     *
     * @param template the path template.
     * @param rhpp the right hand path pattern postfix.
     */
    public JerseyPathPattern(JerseyPathTemplate template, RightHandPath rhpp) {
        super(postfixWithCapturingGroup(template.getJerseyPattern().getRegex(), rhpp),
                addIndexForRightHandPathCapturingGroup(template.getJerseyPattern().getGroupIndexes()));

        this.template = template;
    }

    public JerseyUriTemplate getTemplate() {
        return template;
    }

    private static String postfixWithCapturingGroup(String regex) {
        return postfixWithCapturingGroup(regex, RightHandPath.capturingZeroOrMoreSegments);
    }

    private static String postfixWithCapturingGroup(String regex, RightHandPath rhpp) {
        if (regex.endsWith("/")) {
            regex = regex.substring(0, regex.length() - 1);
        }

        return regex + rhpp.getRegex();
    }

    private static int[] addIndexForRightHandPathCapturingGroup(int[] indexes) {
        if (indexes.length == 0) {
            return indexes;
        }

        int[] cgIndexes = new int[indexes.length + 1];
        System.arraycopy(indexes, 0, cgIndexes, 0, indexes.length);

        cgIndexes[indexes.length] = cgIndexes[indexes.length - 1] + 1;
        return cgIndexes;
    }
}
