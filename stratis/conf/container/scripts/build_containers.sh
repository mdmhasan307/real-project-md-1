#!/bin/sh

if [ -z "$REGISTRY_HOST" ]
then
        echo "The REGISTRY_HOST variable must be set in the build configuration."
	exit -1
fi

if [ -z "$REGISTRY_PATH" ]
then
        echo "The REGISTRY_PATH variable must be set in the build configuration."
	exit -1
fi

if [ -z "$ARTIFACT_NAME" ]
then
        echo "The ARTIFACT_NAME variable must be set in the build configuration."
	exit -1
fi

export ARTIFACT=$REGISTRY_HOST/$REGISTRY_PATH/$ARTIFACT_NAME
export ARTIFACT_SHA=$ARTIFACT:$CI_COMMIT_SHA
export ARTIFACT_TAG=$ARTIFACT:$RELEASE_VERSION

echo "Running the Container Build for the MLS2 STRATIS Application.. (Commit: $CI_COMMIT_SHA)"
echo "Running with Docker Registry Host: $REGISTRY_HOST"

echo "Executing Docker Registry Login, via CI-Based Credentials.."
echo $REGISTRY_TOKEN | docker login -u robot\$mls2_ci --password-stdin $REGISTRY_HOST

echo "Starting Container Build against ARTIFACT_TAG and SHA Tags.."
docker build --no-cache -t $ARTIFACT_SHA -t $ARTIFACT_TAG .
echo "Completed the MLS2 STRATIS Application Container Build.."

echo "Inspecting the created container for validation.."
docker inspect $ARTIFACT_TAG

echo "Completed the STRATIS Container Inspection.."
