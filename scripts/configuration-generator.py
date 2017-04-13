#!/usr/bin/python

# grab the curie mapping
# scan the current directory for the tsvs
# output an owlsim config file

import yaml
import urllib2
import fnmatch
import os

def main():

    # Getting tsv file path
    tsvs = []
    for root, _, filenames in os.walk('.'):
        for filename in fnmatch.filter(filenames, '*.tsv'):
            tsvs.append(os.path.abspath(os.path.join(root, filename))) # Check that: it looks insane to get the absolute path...

    dataTsvs = [filename for filename in tsvs if not filename.endswith('label.tsv')]
    labelTsvs = [filename for filename in tsvs if filename.endswith('label.tsv')]

    # Note: the yaml dump is buggy with the empty string key, so I'm just appending the raw string and add indentation manually
    curies = urllib2.urlopen("https://raw.githubusercontent.com/monarch-initiative/dipper/master/dipper/curie_map.yaml").read()

    data = dict(
        ontologyUris = ['http://purl.obolibrary.org/obo/upheno/monarch.owl'],
        ontologyDataUris = [],
        dataTsvs = dataTsvs,
        labelTsvs = labelTsvs
    )

    print("Generating owlsim configuration")
    with open('configuration.yaml', 'w') as outfile:
        yaml.dump(data, outfile, default_flow_style=False)
        outfile.write("curies:\n")
        outfile.write('  '.join(curies.splitlines(True)))


if __name__ == "__main__":
    main()
