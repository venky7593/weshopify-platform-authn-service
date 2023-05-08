# http://15.207.64.68:8082/artifactory/libs-snapshot-local/
#com/durgasoft/weshopify-platform-services-registry/
ARTIFACT_SERVER_URL=http://15.207.64.68:8082/artifactory
ARTIFACT_REPO_NAME=libs-snapshot-local
ARTIFACT_LOC=com/durgasoft/weshopify-platform-services-registry
ARTIFACT_VERSION=0.0.1-SNAPSHOT

#http://15.207.64.68:8082/artifactory/libs-snapshot-local/
#com/durgasoft/weshopify-platform-services-registry/0.0.1-SNAPSHOT
ARTIFACT_REPO_URL=$ARTIFACT_SERVER_URL/$ARTIFACT_REPO_NAME/$ARTIFACT_LOC/$ARTIFACT_VERSION
ARTIFACT_META_XML=maven-metadata.xml
ARTIFACT_META_XML_URL=$ARTIFACT_REPO_URL/$ARTIFACT_META_XML

ARTIFACTORY_USER=admin
ARTIFACTORY_PASSWORD=Adance123$
ARTIFACT_NAME=weshopify-authn-service
ARTIFACTORY_ARTIFACT_NAME=weshopify-authn-service

ARTIFACT_VERSION_WITH_TIME_STAMP=$(curl -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD $ARTIFACT_META_XML_URL | grep 'value' | head -1 | sed "s/.*<value>\([^<]*\)<\/value>.*/\1/")
echo 'ARTIFACT_VERSION_WITH_TIME_STAMP' $ARTIFACT_VERSION_WITH_TIME_STAMP
#http://15.207.64.68:8082/artifactory
#libs-snapshot-local
#com/durgasoft/weshopify-platform-services-registry
#0.0.1-SNAPSHOT/
#weshopify-platform-services-registry-
#0.0.1-20230415.034533-10.jar
FINAL_ARTIFACT_URL=$ARTIFACT_REPO_URL/$ARTIFACTORY_ARTIFACT_NAME-$ARTIFACT_VERSION_WITH_TIME_STAMP.jar
echo 'FINAL_ARTIFACT_URL' $FINAL_ARTIFACT_URL

wget --user=$ARTIFACTORY_USER --password=$ARTIFACTORY_PASSWORD $FINAL_ARTIFACT_URL
echo 'FINAL_ARTIFACT_DOWNLOADED' $FINAL_ARTIFACT_URL
cp $ARTIFACTORY_ARTIFACT_NAME-$ARTIFACT_VERSION_WITH_TIME_STAMP.jar ./app-dir/target/$ARTIFACT_NAME.jar
rm $ARTIFACTORY_ARTIFACT_NAME-*.jar