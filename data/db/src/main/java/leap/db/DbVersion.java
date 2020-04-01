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

public class DbVersion {
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
}
