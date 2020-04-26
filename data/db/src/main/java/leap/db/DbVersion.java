/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.db;

import leap.lang.Strings;

import java.util.Comparator;

public class DbVersion {
    public static Comparator<DbVersion> SORT_COMPARATOR = (o1, o2) -> !o1.ge(o2) ? -1 : 1;

    public static DbVersion parseDot(String v) {
        final String[] parts = v.indexOf('.') > 0 ? Strings.split(v, '.') : new String[]{v};
        return parse(v, parts);
    }

    public static DbVersion parseUnderscore(String v) {
        final String[] parts = v.indexOf('_') > 0 ? Strings.split(v, '_') : new String[]{v};
        return parse(v, parts);
    }

    private static DbVersion parse(String v, String[] parts) {
        if(parts.length < 2 || parts.length > 3) {
            throw new IllegalStateException("Invalid version '" + v + "'");
        }
        try {
            int major    = Integer.parseInt(parts[0]);
            int minor    = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int revision = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            return of(major, minor, revision);
        }catch (Exception e) {
            throw new IllegalStateException("Invalid version '" + v + "'");
        }
    }

    public static DbVersion of(int major, int minor, int revision) {
        return new DbVersion(major, minor, revision);
    }

    protected final int major;
    protected final int minor;
    protected final int revision;

    public DbVersion(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public boolean ge(DbVersion ver) {
        if(major < ver.major) {
            return false;
        }
        if(major > ver.major) {
            return true;
        }
        if(minor < ver.minor) {
            return false;
        }
        if(minor > ver.minor) {
            return true;
        }
        if(revision < ver.revision) {
            return false;
        }
        return true;
    }

    public String getDotExpr() {
        return major + "." + minor + "." + revision;
    }
}
