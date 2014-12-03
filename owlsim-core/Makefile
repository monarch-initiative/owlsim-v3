OBO := http://purl.obolibrary.org/obo

all: src/test/resources/ontologies src/test/resources/ontologies/hp.obo src/test/resources/data/omim-disease-phenotype.owl src/test/resources/data/disorders.ttl

src/test/resources/ontologies:
	mkdir $@

src/test/resources/ontologies/%.obo:
	wget $(OBO)/$*.obo -O $@

src/test/resources/data/%.owl:
	wget $(OBO)/upheno/data/$*.owl  -O $@

src/test/resources/data/%.ttl:
	wget $(OBO)/upheno/data/$*.ttl  -O $@
