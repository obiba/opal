# Connect and setup environment in CaG Study
cag=opal.login("http://localhost:8080", "plaflamme", "password");
datashield.assign(cag, BMI="opal-data.Impedance418:RES_BODY_MASS_INDEX", AGE="opal-data.Impedance418:INPUT_PARTICIPANT_AGE", GENDER="opal-data.Impedance418:INPUT_PARTICIPANT_GENDER");

# Connect and setup environment in OHS Study
ohs=opal.login("http://localhost:8080", "plaflamme", "password");
datashield.assign(ohs, BMI="opal-data.Impedance418:RES_BODY_MASS_INDEX", AGE="opal-data.Impedance418:INPUT_PARTICIPANT_AGE", GENDER="opal-data.Impedance418:INPUT_PARTICIPANT_GENDER");

studies<-c(cag,ohs)
# Give them a name
names(studies)<-list("CaG", "OHS")

# Count the number of participants in each study
numsubs.study=datashield.length(studies, "result=BMI")

numstudies=length(studies)
numpara<-3

#create empty results matrices
beta.s<-matrix(NA,nrow=numpara,ncol=numstudies)
se.s<-matrix(NA,nrow=numpara,ncol=numstudies)

#set up analysis weights
analysis.wt<-numsubs.study/numstudies

#set up a vector of 1s to use in summing precisions
simple.sum<-rep(1,numstudies)

# Perform regression in each study
model.study.specific = datashield.summary(studies, "result=lm(BMI ~ AGE + GENDER)")

# Populate result matrices
for(k in c(1:numstudies)) {
  beta.s[,k] = model.study.specific[[k]]$coefficients[,1]
  se.s[,k]=model.study.specific[[k]]$coefficients[,2]
}

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
