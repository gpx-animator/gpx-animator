FROM gitpod/workspace-full-vnc
USER root
RUN apt-get update \
    && bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && sdk install java 15.0.1.hs-adpt" \
    && apt-get install -y openjfx libopenjfx-java matchbox \
    && apt-get clean && rm -rf /var/cache/apt/* && rm -rf /var/lib/apt/lists/* && rm -rf /tmp/*
