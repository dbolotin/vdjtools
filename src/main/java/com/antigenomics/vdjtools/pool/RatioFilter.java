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

package com.antigenomics.vdjtools.pool;

import com.antigenomics.vdjtools.sample.Clonotype;
import com.antigenomics.vdjtools.sample.ClonotypeFilter;
import com.antigenomics.vdjtools.sample.Sample;

public class RatioFilter extends ClonotypeFilter {
    private final SampleAggregator<MaxClonotypeAggregator> sampleAggregator;
    private final double thresholdRatio;

    public RatioFilter(Iterable<Sample> samples, double thresholdRatio, boolean negative) {
        super(negative);
        this.sampleAggregator = new SampleAggregator<>(samples, new MaxClonotypeAggregatorFactory());
        this.thresholdRatio = thresholdRatio;
    }

    public RatioFilter(Iterable<Sample> samples, double thresholdRatio) {
        this(samples, thresholdRatio, false);
    }

    public RatioFilter(Iterable<Sample> samples) {
        this(samples, 20.0);
    }

    @Override
    protected boolean checkPass(Clonotype clonotype) {
        return sampleAggregator.getAt(clonotype).getMaxFreq() < clonotype.getFreq() * thresholdRatio;
    }
}
