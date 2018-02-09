import subprocess
from os.path import join
import os

def write_config(config, filepath):
    with open(filepath, "w") as f:
        for elem in config:
            f.write("%s = %s\n" % (elem, config[elem]))


def main():
    results_folder = "results"
    base_config = "config"

    # dataset parameter files
    files = ["multiling.conf", "ng20.conf"]

    # representations compatible as per the jedai framework
    # bow and tftdf-bow, unigrams to trigrams
    representations = ["bow_w%d" % i for i in range(1,4)]
    representations += ["bow_tfidf_w%d" % i for i in range(1,4)]
    # word ng graphs, from uni to trigrams
    representations += ["ngg_w%d" % i for i in range(1,4)]
    # character ng graphs, from bi to fourgrams
    representations += ["ngg_c%d" % i for i in range(2,5)]

    # compatible similarities per representation
    sims = {"bow":["cosine", "jaccard"], "ngg":["nvs"]}

    # do the work
    ######################
    datasets = []
    for file in files:
        with open(file) as f:
            dset = {}
            for line in f:
                line = line.strip()
                if not line: continue
                name, value = line.split(":")
                dset[name] = value
        dset["name"] = file
        datasets.append(dset)


    if not os.path.exists(results_folder):
        os.mkdir(results_folder)

    config = {}
    config["verbosity"] = "false"
    for dset in datasets:
        files_gt = dset["files_gt"]
        topics_gt = dset["topics_gt"]
        results_dset_folder = join(results_folder, dset["results"])
        if not os.path.exists(results_dset_folder):
            os.mkdir(results_dset_folder)

        for read_mode in ["texts", "entities"]:
            config["read_order"] = files_gt
            config["input_path"] = dset[read_mode]
            config["read_mode"] = read_mode

            for repr in representations:
                config["representation"] = repr
                repr_prefix = repr.split("_")[0]
                sims_list = sims[repr_prefix]
                for sim in sims_list:
                    config["similarity"] = sim

                    config_id = ".".join([dset["name"], read_mode, repr, sim])
                    config_file = join(results_dset_folder, "config." + config_id)
                    output_file = join(results_dset_folder, "results." + config_id)
                    write_config(config, config_file)

                    # run!
                    cmds = ["./execute.sh", config_file]
                    print(cmds)
                    # subprocess.run(cmd, stdout = output_file)


if __name__ == "__main__":
    main()
