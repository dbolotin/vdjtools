require(VennDiagram); require(RColorBrewer)

args<-commandArgs(TRUE)

area1    <- as.integer(args[1])
area2    <- as.integer(args[2])
area3    <- as.integer(args[3])
area4    <- as.integer(args[4])
area5    <- as.integer(args[5])

n12345   <- as.integer(args[31])

# yes we have to integrate so that the VennDiagram will recalculate back
# no option to provide raw counts unfortunately
# haven't found any other package that accepts collapsed counts
# (imagine writing to file and loading 20mln binary strings to R)

n1234    <- as.integer(args[26])+n12345
n1235    <- as.integer(args[27])+n12345
n1245    <- as.integer(args[28])+n12345
n1345    <- as.integer(args[29])+n12345
n2345    <- as.integer(args[30])+n12345

n123     <- as.integer(args[16])+n1234+n1235
n124     <- as.integer(args[17])+n1234+n1245
n125     <- as.integer(args[18])+n1235+n1245
n134     <- as.integer(args[19])+n1234+n1345
n135     <- as.integer(args[20])+n1235+n1345
n145     <- as.integer(args[21])+n1245+n1345
n234     <- as.integer(args[22])+n1234+n2345
n235     <- as.integer(args[23])+n1235+n2345
n245     <- as.integer(args[24])+n1245+n2345
n345     <- as.integer(args[25])+n1345+n2345

n12      <- as.integer(args[6])+n123+n124+n125
n13      <- as.integer(args[7])+n123+n134+n135
n14      <- as.integer(args[8])+n124+n134+n145
n15      <- as.integer(args[9])+n125+n135+n145
n23      <- as.integer(args[10])+n123+n234+n235
n24      <- as.integer(args[11])+n124+n234+n245
n25      <- as.integer(args[12])+n125+n235+n245
n34      <- as.integer(args[13])+n134+n234+n345
n35      <- as.integer(args[14])+n135+n235+n345
n45      <- as.integer(args[15])+n145+n245+n345

category <- strsplit(args[32],split=",")[[1]]
file_out <- args[33]

cols <- brewer.pal(5, "Paired")

if (grepl("\\.pdf$",file_out)){
   pdf(file_out)
} else if (grepl("\\.png$",file_out)) {
   png(file_out, width     = 3.25,
                 height    = 3.25,
                 units     = "in",
                 res       = 1200,
                 pointsize = 4)
} else {
   stop('Unknown plotting format')
}

if (is.na(area3)) {
  dummy <- draw.pairwise.venn(area1, area2,
                                  n12,
                                  category[1:2], fill=cols[1:2], alpha=rep(0.5,2),
                                  col=rep("gray50",2), lwd=rep(1,2), margin=0.1)
} else if (is.na(area4)) {
  dummy <- draw.triple.venn(area1, area2, area3,
                                n12, n23, n13,
                                n123,
                                category[1:3], fill=cols[1:3], alpha=rep(0.5,3),
                                col=rep("gray50",3), lwd=rep(1,3), margin=0.1)
} else if (is.na(area5)) {
  dummy <- draw.quad.venn(area1, area2, area3, area4,
                              n12, n13, n14, n23, n24, n34,
                              n123, n124,
                              n134, n234, n1234,
                              category[1:4], fill=cols[1:4], alpha=rep(0.5,4),
                              col=rep("gray50",4), lwd=rep(1,4), margin=0.1)
} else {
  dummy <- draw.quintuple.venn(area1, area2, area3, area4, area5,
                                   n12, n13, n14, n15, n23, n24, n25,
                                   n34, n35, n45, n123, n124, n125, n134,
                                   n135, n145, n234, n235, n245, n345,
                                   n1234, n1235, n1245, n1345, n2345, n12345,
                                   category[1:5], fill=cols[1:5], alpha=rep(0.5,5), 
                                   col=rep("gray50",5), lwd=rep(1,5), margin=0.1)
}

dev.off()