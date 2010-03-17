# numstudies
# numpara
# study.coeffs

numsubs.study <- c(91,100)
numsubs <- sum(numsubs.study)

beta.s<-matrix(NA,nrow=numpara,ncol=numstudies)
se.s<-matrix(NA,nrow=numpara,ncol=numstudies)

for(k in 1:numstudies) {
  beta.s[,k]<-study.coeffs[[k]][,1]
  se.s[,k]<-study.coeffs[[k]][,2]
}

#################
# META-ANALYSIS #
#################
#set up analysis weights
analysis.wt<-numsubs.study/numsubs

#set up a vector of 1s to use in summing precisions
simple.sum<-rep(1,numstudies)

#calculate mean of regression coefficients weighted for study sample sizes
# “%*%” denotes vector multiplication
beta.overall<-beta.s%*%analysis.wt

#convert standard errors into precisions
precision.s <-1/(se.s)^2

#sum precisions across studies
precision.overall<-precision.s%*%simple.sum

#convert precisions back to standard errors
se.overall <-1/(precision.overall)^0.5

#round outputs
beta.overall<-round(beta.overall,digits=4)
se.overall<-round(se.overall,digits=4)

#create output results matrix
meta.analysis.results<-cbind(beta.overall,se.overall)
dimnames(meta.analysis.results)<-list(c("Intercept","AGE","GENDER"),c("Coefficients","SE"))

#print output
print(analysis.wt)
print(meta.analysis.results)
