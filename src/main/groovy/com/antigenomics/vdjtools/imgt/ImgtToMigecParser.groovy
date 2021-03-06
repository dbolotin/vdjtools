/**
 Copyright 2014 Mikhail Shugay (mikhail.shugay@gmail.com)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.antigenomics.vdjtools.imgt

import static com.antigenomics.vdjtools.util.CommonUtil.getCYS_REGEX
import static com.antigenomics.vdjtools.util.CommonUtil.getJReferencePoint

class ImgtToMigecParser {
    final static int IMGT_V_REF = 312, IMGT_V_REF_AUX = 318 // Only way to handle the mess
    final boolean onlyFunctional, onlyMajorAllele

    final List<ImgtRecord> failedVReferencePoint = new ArrayList<>(),
                           failedJReferencePoint = new ArrayList<>(),
                           otherSegment = new ArrayList<>()

    final Map<String, Map<String, boolean[]>> segmentPresence = new HashMap<>()

    ImgtToMigecParser(boolean onlyFunctional, boolean onlyMajorAllele) {
        this.onlyFunctional = onlyFunctional
        this.onlyMajorAllele = onlyMajorAllele
    }

    static String getGene(ImgtRecord imgtRecord) {
        imgtRecord.fullId.substring(0, 3)
    }

    static String sequenceNoGaps(ImgtRecord record) {
        record.sequence.replaceAll("\\.", "")
    }

    static boolean majorAllele(ImgtRecord record) {
        record.allele == "01"
    }

    static boolean functional(ImgtRecord record) {
        record.functionality == "F"
    }

    static int getVReferencePoint(ImgtRecord imgtRecord) {
        String sequenceWithGaps = imgtRecord.sequence

        //if (IMGT_V_REF > sequenceWithGaps.length())
        //    return -1

        def ref = [IMGT_V_REF, IMGT_V_REF_AUX].find { ref ->
            if (ref <= sequenceWithGaps.length()) {
                String codon = sequenceWithGaps.substring(ref - 3, ref)
                return codon =~ CYS_REGEX
            }
            false
        }

        if (ref) {
            int imgtGapsCount = sequenceWithGaps.substring(0, ref).count(".")
            return ref - imgtGapsCount
        }

        return -1
    }

    static int getJReferencePoint(ImgtRecord imgtRecord) {
        String sequenceWithoutGaps = sequenceNoGaps(imgtRecord) // just in case
        getJReferencePoint(sequenceWithoutGaps)
    }

    MigecSegmentRecord createRecord(ImgtRecord imgtRecord, String gene, String segment, int referencePoint) {
        new MigecSegmentRecord(imgtRecord.species, gene,
                segment, imgtRecord.fullId, sequenceNoGaps(imgtRecord),
                referencePoint)
    }

    MigecSegmentRecord parseRecord(ImgtRecord imgtRecord) {
        def gene = getGene(imgtRecord)

        def geneSegmentPresenceMap = segmentPresence[imgtRecord.species]
        if (!geneSegmentPresenceMap)
            segmentPresence.put(imgtRecord.species, geneSegmentPresenceMap = new HashMap<String, boolean[]>())

        def segmentPresenceArr = geneSegmentPresenceMap[gene]
        if (!segmentPresenceArr)
            geneSegmentPresenceMap.put(gene, segmentPresenceArr = new boolean[3])

        if ((!onlyFunctional || functional(imgtRecord)) &&
                (!onlyMajorAllele || majorAllele(imgtRecord))) {
            switch (imgtRecord.type) {
                case "V-REGION":
                    int vReferencePoint = getVReferencePoint(imgtRecord)
                    if (vReferencePoint < 0) {
                        failedVReferencePoint.add(imgtRecord)
                        return null
                    }
                    segmentPresenceArr[0] = true
                    return createRecord(imgtRecord, gene, "Variable", vReferencePoint)
                case "D-REGION":
                    segmentPresenceArr[1] = true
                    return createRecord(imgtRecord, gene, "Diversity", -1)
                case "J-REGION":
                    int jReferencePoint = getJReferencePoint(imgtRecord)
                    if (jReferencePoint < 0) {
                        failedJReferencePoint.add(imgtRecord)
                        return null
                    }
                    segmentPresenceArr[2] = true
                    return createRecord(imgtRecord, gene, "Joining", jReferencePoint)
            }
        }
        otherSegment.add(imgtRecord)
        null
    }

    static final String HEADER = "#SPECIES\tGENE\tV\tD\tJ\tVJ"

    @Override
    String toString() {
        segmentPresence.entrySet().collect { bySpecies ->
            bySpecies.value.entrySet().collect { byGene ->
                [bySpecies.key, byGene.key, byGene.value.collect {
                    it ? 1 : 0
                }, byGene.value[0] && byGene.value[2] ? 1 : 0].flatten().join("\t")
            }.join("\n")
        }.join("\n")
    }
}
