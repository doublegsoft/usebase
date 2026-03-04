export MODELBASE_DATA_ROOT=/Users/christian/export/local/works/doublegsoft.io/modelbase/03.Development/modelbase-data
export USEBAS_DATA_ROOT=/Users/christian/export/local/works/doublegsoft.io/usebase/03.Development/usebase-data
export MODELBASE_JAR=/Users/christian/export/opt/modelbase/protosys-plugin-modelbase-6.0-shaded.jar
export PROJBASE_DATA_ROOT=/Users/christian/export/local/works/doublegsoft.io/projbase/03.Development/projbase-data
export PROJBASE_JAR=/Users/christian/export/opt/projbase/protosys-plugin-projbase-4.5-shaded.jar
export OUTPUT_ROOT=out/java

export SPEC="wfm"
export APPNAME=wfm
export NAMESPACE=biz.doublegsoft
export MODELBASE_MODEL="src/test/resources/model/$SPEC.modelbase"
export PROJECT_ROOT=$OUTPUT_ROOT/usebase-env-java
################################################################################
##                                                                            ##
##                                     JAVA                                   ##
##                                                                            ##
################################################################################
REPOS=("java-poco@gfc-1.x" "java-util@gfc-1.x" "java-dto@gfc-1.x" "java-orm@mybatis-1.x" "java-orm@rdbms-1.x" \
"java-tx@spring-1.x" "java-runtime@springboot-1.x")

for repo in "${REPOS[@]}"
do
export TEMPLATE_ROOT=$MODELBASE_DATA_ROOT/java/$repo

java -jar $MODELBASE_JAR \
--model=$MODELBASE_MODEL \
--template-root=$TEMPLATE_ROOT \
--output-root=$PROJECT_ROOT \
--license=env/LICENSE \
--globals=\
\{\
\"application\":\"$APPNAME\",\
\"namespace\":\"$NAMESPACE\",\
\"artifact\":\"$APPNAME\",\
\"version\":\"1.0.0\",\
\"description\":\"\",\
\"naming\":\"com.doublegsoft.jcommons.programming.java.JavaConventions\",\
\"globalNamingConvention\":\"com.doublegsoft.jcommons.programming.java.JavaNamingConvention\",\
\"language\":\"java\",\
\"imports\":\
\[\],\
\"dependencies\":\
\[\]\
\} 2>&1
done

################################################################################
##                                                                            ##
##                    MODELBASE PROPAGATION BY USEBASE                        ##
##                                                                            ##
################################################################################
REPOS=("java-dto@gfc-1.x")

export MODELBASE_MODEL=out/usebase/"$SPEC".modelbase

for repo in "${REPOS[@]}"
do
export TEMPLATE_ROOT=$USEBAS_DATA_ROOT/java/$repo

java -jar $MODELBASE_JAR \
--model=$MODELBASE_MODEL \
--template-root=$TEMPLATE_ROOT \
--output-root=$PROJECT_ROOT \
--license=env/LICENSE \
--globals=\
\{\
\"application\":\"$APPNAME\",\
\"namespace\":\"$NAMESPACE\",\
\"artifact\":\"$APPNAME\",\
\"version\":\"1.0.0\",\
\"description\":\"\",\
\"naming\":\"com.doublegsoft.jcommons.programming.java.JavaConventions\",\
\"globalNamingConvention\":\"com.doublegsoft.jcommons.programming.java.JavaNamingConvention\",\
\"language\":\"java\",\
\"imports\":\
\[\],\
\"dependencies\":\
\[\]\
\} 2>&1
done

cp -f env/java/pom.xml $PROJECT_ROOT/pom.xml
mvn clean package -f $PROJECT_ROOT/pom.xml -DskipTests