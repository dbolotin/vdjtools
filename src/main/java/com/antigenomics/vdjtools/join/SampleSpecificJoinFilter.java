/*
 * Copyright 2013-2015 Mikhail Shugay (mikhail.shugay@gmail.com)
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

package com.antigenomics.vdjtools.join;

public class SampleSpecificJoinFilter implements JoinFilter {
    private final int sampleIndex;

    public SampleSpecificJoinFilter() {
        this(0);
    }

    public SampleSpecificJoinFilter(int sampleIndex) {
        this.sampleIndex = sampleIndex;
    }

    public int getSampleIndex() {
        return sampleIndex;
    }

    @Override
    public boolean pass(JointClonotype jointClonotype) {
        return jointClonotype.present(sampleIndex);
    }
}
