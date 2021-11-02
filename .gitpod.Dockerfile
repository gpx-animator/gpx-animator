FROM gitpod/workspace-full-vnc
USER root
RUN apt-get update \
    && bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && sdk install java 17.0.1-zulu" \
    && bash -c 'echo export JAVA_TOOL_OPTIONS=\"\$JAVA_TOOL_OPTIONS -Dsun.java2d.xrender=false\" >> /home/gitpod/.bashrc' \
    && apt-get install -y openjfx libopenjfx-java matchbox \
    && apt-get clean && rm -rf /var/cache/apt/* && rm -rf /var/lib/apt/lists/* && rm -rf /tmp/*
    
