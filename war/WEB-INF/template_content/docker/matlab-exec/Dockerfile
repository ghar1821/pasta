FROM openjdk:8-jdk

RUN apt-get update && apt-get install -y --no-install-recommends ant ant-optional time coreutils

ARG workDir

RUN mkdir ${workDir}/
WORKDIR ${workDir}/

RUN useradd -c 'PASTA Sandbox User' -m -b /home/ -s /bin/bash -u 1001 pastasandbox

ARG binDir

COPY bin/ ${binDir}/
RUN chmod 755 ${binDir}/ # Needs 755 permissions as non-privileged users need to be able to access sub-directories of bin.
RUN chmod 4750 ${binDir}/matlabpastarun
