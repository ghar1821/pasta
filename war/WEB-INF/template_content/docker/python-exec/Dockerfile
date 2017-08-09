FROM openjdk:8-jdk

RUN apt-get update && apt-get install -y --no-install-recommends ant ant-optional time coreutils python3-dev python3-pip

ARG workDir

RUN mkdir ${workDir}/
WORKDIR ${workDir}/

RUN useradd -c 'PASTA Sandbox User' -M -s /bin/bash -u 1001 pastasandbox

ARG binDir

COPY bin/ ${binDir}/
RUN chmod 750 ${binDir}/
RUN chmod 4750 ${binDir}/pastarun