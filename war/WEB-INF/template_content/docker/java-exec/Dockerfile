FROM openjdk:8-jdk

RUN apt-get update && apt-get install -y --no-install-recommends ant ant-optional time coreutils

RUN mkdir /sandbox/
WORKDIR /sandbox/

RUN useradd -c 'PASTA Sandbox User' -M -s /bin/bash -u 1001 pastasandbox

COPY bin/ /pasta/bin/
RUN chmod 750 /pasta/bin/
RUN chmod 4750 /pasta/bin/pastarun