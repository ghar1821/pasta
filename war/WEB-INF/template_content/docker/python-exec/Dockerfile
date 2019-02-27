FROM openjdk:8-jdk

RUN apt-get update && apt-get install -y --no-install-recommends ant ant-optional time coreutils

# Required to install Python from source 
RUN apt-get update && apt-get install -y --no-install-recommends gcc libc6-dev make libssl-dev openssl zlib1g-dev libffi-dev

ARG pythonVersion

RUN if [ -z "${pythonVersion}" ]; then apt-get install -y --no-install-recommends python3-dev python3-pip; else wget "https://www.python.org/ftp/python/${pythonVersion}/Python-${pythonVersion}.tgz"; tar zxf "Python-${pythonVersion}.tgz"; cd "Python-${pythonVersion}"; ./configure; make; make install; cd ..; rm -rf "Python-${pythonVersion}"; ln -s /usr/local/bin/python3 /usr/bin/python3; ln -s /usr/local/bin/pip3 /usr/bin/pip3; fi

ARG pipPackages

RUN if [ ! -z "${pipPackages}" ]; then pip3 install ${pipPackages} ; fi

ARG workDir

RUN mkdir ${workDir}/
WORKDIR ${workDir}/

RUN useradd -c 'PASTA Sandbox User' -M -s /bin/bash -u 1001 pastasandbox

ARG binDir

COPY bin/ ${binDir}/
RUN chmod 750 ${binDir}/
RUN chmod 4750 ${binDir}/pastarun
