FROM docker.adeo.no:5000/bekkci/maven-builder
# [ ARG STEP_MARKER ] se http://stash.devillo.no/projects/BEKKCI/repos/jenkins-plugin/browse
ARG STEP_MARKER

ADD / /source
RUN build


# TODO oppsett for nais
