import subprocess,os, datetime
from os.path import join


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
    # clustering approaches
    clustering = ["ricochet"]
    # clustering thresholds
    clust_thresholds = [str(x/10) for x in list(range(1,10))]

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
    timings = {}
    config["verbosity"] = "false"

    for dset in datasets[:1]:
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

                    for clust in clustering:
                        config["clustering"] = clust

                        for c_thresh in clust_thresholds:
                            config["clustering_threshold"] = c_thresh

                            config_id = ".".join([dset["name"], read_mode, repr, sim, clust, c_thresh])
                            config_file = join(results_dset_folder, "config." + config_id)
                            output_file = join(results_dset_folder, "results." + config_id)
                            write_config(config, config_file)

                            # run!
                            cmd = ["./execute.sh", config_file]
                            print(cmd)
                            timestart = datetime.datetime.now()
                            """
                            with open(output_file,"w") as ofile:
                                subprocess.run(cmd, stdout = ofile)
                            """
                            timings[config_id] = (datetime.datetime.now() - timestart).seconds

if __name__ == "__main__":
    main()
