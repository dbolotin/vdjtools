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

package com.antigenomics.vdjtools.operate

import com.antigenomics.vdjtools.Software
import com.antigenomics.vdjtools.io.SampleWriter
import com.antigenomics.vdjtools.overlap.OverlapType
import com.antigenomics.vdjtools.pool.PooledSample
import com.antigenomics.vdjtools.pool.SampleAggregator
import com.antigenomics.vdjtools.pool.StoringClonotypeAggregatorFactory
import com.antigenomics.vdjtools.sample.SampleCollection
import com.antigenomics.vdjtools.util.ExecUtil

import static com.antigenomics.vdjtools.util.ExecUtil.formOutputPath

def I_TYPE_DEFAULT = "aa"
def cli = new CliBuilder(usage: "PoolSamples [options] " +
        "[sample1 sample2 sample3 ... if not -m] output_prefix")
cli.h("display help message")
cli.m(longOpt: "metadata", argName: "filename", args: 1,
        "Metadata file. First and second columns should contain file name and sample id. " +
                "Header is mandatory and will be used to assign column names for metadata.")
cli.i(longOpt: "intersect-type", argName: "string", args: 1,
        "Comma-separated list of overlap types to apply. " +
                "Allowed values: $OverlapType.allowedNames. " +
                "Will use '$I_TYPE_DEFAULT' by default.")
cli.c(longOpt: "compress", "Compress output sample files.")

def opt = cli.parse(args)

if (opt == null) {
    //cli.usage()
    System.exit(-1)
}

if (opt.h) {
    cli.usage()
    System.exit(0)
}

// Check if metadata is provided

def metadataFileName = opt.m

if (metadataFileName ? opt.arguments().size() != 1 : opt.arguments().size() < 3) {
    if (metadataFileName)
        println "Only output prefix should be provided in case of -m"
    else
        println "At least 2 sample files should be provided if not using -m"
    cli.usage()
    System.exit(-1)
}

def outputPrefix = opt.arguments()[-1],
    compress = (boolean) opt.c

ExecUtil.ensureDir(outputPrefix)

def scriptName = getClass().canonicalName.split("\\.")[-1]

// Select overlap type

def iName = opt.i ?: I_TYPE_DEFAULT
def intersectionType = OverlapType.getByShortName(iName)

if (!intersectionType) {
    println "[ERROR] Bad overlap type specified ($iName). " +
            "Allowed values are: $OverlapType.allowedNames"
    System.exit(-1)
}

//
// Batch load all samples
//

println "[${new Date()} $scriptName] Reading samples"

def sampleCollection = metadataFileName ?
        new SampleCollection((String) metadataFileName, Software.VDJtools, false, true) :
        new SampleCollection(opt.arguments()[0..-2], Software.VDJtools, false, true)

println "[${new Date()} $scriptName] ${sampleCollection.size()} samples loaded"

//
// Pool samples
//

println "[${new Date()} $scriptName] Pooling with $intersectionType, this may take a while"

def cloneAggrFact = new StoringClonotypeAggregatorFactory()

def sampleAggr = new SampleAggregator(sampleCollection, cloneAggrFact, intersectionType)

//
// Sort pooled sample and write output
//

println "[${new Date()} $scriptName] Normalizing and sorting pooled clonotypes"
def pooledSample = new PooledSample(sampleAggr)

println "[${new Date()} $scriptName] Writing output"
def writer = new SampleWriter(compress)
writer.write(pooledSample, formOutputPath(outputPrefix, "pool", intersectionType.shortName, "table"))

println "[${new Date()} $scriptName] Finished."


