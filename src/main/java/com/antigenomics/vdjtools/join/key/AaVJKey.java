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

package com.antigenomics.vdjtools.join.key;

import com.antigenomics.vdjtools.sample.Clonotype;

public final class AaVJKey extends ClonotypeKey {
    public AaVJKey(Clonotype clonotype) {
        super(clonotype);
    }

    @Override
    public boolean equals(Clonotype other) {
        return clonotype.getCdr3aaBinary().equals(other.getCdr3aaBinary()) &&
                clonotype.getVBinary().equals(other.getVBinary()) &&
                clonotype.getJBinary().equals(other.getJBinary());
    }

    @Override
    public int hashCode() {
        return 31 * (clonotype.getCdr3aaBinary().hashCode() * 31 + clonotype.getVBinary().hashCode()) +
                clonotype.getJBinary().hashCode();
    }
}
