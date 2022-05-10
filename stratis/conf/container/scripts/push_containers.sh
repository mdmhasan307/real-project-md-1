#!/bin/sh

export ARTIFACT=$REGISTRY_HOST/$REGISTRY_PATH/$ARTIFACT_NAME
export ARTIFACT_SHA=$ARTIFACT:$CI_COMMIT_SHA
export ARTIFACT_TAG=$ARTIFACT:$RELEASE_VERSION

echo "Running the MLS2 STRATIS Application Container Push.. (Commit $CI_COMMIT_SHA)"
echo "Build Artifact is $ARTIFACT"

echo "Executing Docker Registry Login, via CI-Based Credentials.."
echo $REGISTRY_TOKEN | docker login -u robot\$mls2_ci --password-stdin $REGISTRY_HOST

echo "Pushing MLS2 STRATIS Application Container Images.. (Both Commit and ARTIFACT_TAG Tags)"
docker push $ARTIFACT_SHA
docker push $ARTIFACT_TAG
echo "Completed the MLS2 STRATIS Application Container Push.."

docker pull $ARTIFACT_SHA
echo "Completed the Validation Container Pull.."

exit 0
