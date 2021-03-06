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

package com.antigenomics.vdjtools.sample;

import com.antigenomics.vdjtools.ClonotypeContainer;
import com.antigenomics.vdjtools.Software;
import com.antigenomics.vdjtools.io.parser.ClonotypeStreamParser;
import com.antigenomics.vdjtools.sample.metadata.SampleMetadata;

import java.io.InputStream;
import java.util.*;

public class Sample implements ClonotypeContainer {
    private final List<Clonotype> clonotypes = new ArrayList<>();
    private final SampleMetadata sampleMetadata;
    private double frequency = 0;
    private long count = 0;
    private int diversity = 0;

    private Sample(SampleMetadata sampleMetadata) {
        this.sampleMetadata = sampleMetadata;
    }

    public Sample(Sample toCopy, HashMap<Clonotype, Integer> samplerMap) {
        this.sampleMetadata = toCopy.sampleMetadata;

        for (Clonotype clonotype : toCopy.clonotypes) {
            Integer newCount = samplerMap.get(clonotype);

            if (newCount != null)
                this.addClonotype(new Clonotype(clonotype, this, newCount));
        }

        Collections.sort(clonotypes);
    }

    public Sample(Sample toCopy, ClonotypeFilter filter, int top) {
        this.sampleMetadata = toCopy.sampleMetadata;

        for (Clonotype clonotype : toCopy.clonotypes) {
            if (top > -1 && this.getDiversity() == top)
                break;

            if (filter.pass(clonotype))
                this.addClonotype(new Clonotype(clonotype, this));
        }
    }

    public Sample(Sample toClone, ClonotypeFilter filter) {
        this(toClone, filter, -1);
    }

    public Sample(Sample toClone) {
        this(toClone, BlankClonotypeFilter.INSTANCE, -1);
    }

    public static Sample fromInputStream(InputStream inputStream,
                                         SampleMetadata sampleMetadata,
                                         Software software,
                                         int top, boolean store, boolean collapse) {
        Sample sample = new Sample(sampleMetadata);

        ClonotypeStreamParser clonotypeStreamParser = ClonotypeStreamParser.create(inputStream, software, sample);

        boolean sorted = !collapse;
        int prevCount = Integer.MAX_VALUE;

        Map<Clonotype, Clonotype> existingClonotypes = new HashMap<>();

        for (Clonotype clonotype : clonotypeStreamParser) {
            if (top > -1 && sample.getDiversity() == top)
                break;

            if (clonotype != null) {
                int count = clonotype.getCount();

                if (sorted && count > prevCount) {
                    sorted = false;
                    prevCount = count;
                }

                Clonotype existing = null;

                if (collapse) {
                    existing = existingClonotypes.get(clonotype);

                    if (existing != null) {
                        existing.append(clonotype);
                    } else {
                        existingClonotypes.put(clonotype, clonotype);
                    }
                }

                sample.addClonotype(clonotype, store, existing);
            }
        }

        clonotypeStreamParser.finish(); // report progress

        // on-demand sorting
        if (!sorted)
            Collections.sort(sample.clonotypes);

        // Re-calculate frequencies for per read storing software
        if (software.isPerReadOutput())
            for (Clonotype clonotype : sample)
                clonotype.recalculateFrequency();

        return sample;
    }

    public static Sample fromInputStream(InputStream inputStream,
                                         SampleMetadata sampleMetadata,
                                         Software software) {
        return fromInputStream(inputStream, sampleMetadata, software, -1, true, false);
    }

    private void addClonotype(Clonotype clonotype) {
        addClonotype(clonotype, true, null);
    }

    private void addClonotype(Clonotype clonotype, boolean store, Clonotype existingClonotype) {
        count += clonotype.getCount();
        frequency += clonotype.getFreqAsInInput();

        if (existingClonotype == null) {
            diversity++;
            if (store)
                clonotypes.add(clonotype);
        }
    }

    public SampleMetadata getSampleMetadata() {
        return sampleMetadata;
    }

    @Override
    public double getFreq() {
        return frequency;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public int getDiversity() {
        return diversity;
    }

    @Override
    public Clonotype getAt(int index) {
        if (index < 0 || index >= clonotypes.size())
            throw new IndexOutOfBoundsException();
        return clonotypes.get(index);
    }

    @Override
    public boolean isSorted() {
        return true;
    }

    @Override
    public Iterator<Clonotype> iterator() {
        return clonotypes.iterator();
    }
}
