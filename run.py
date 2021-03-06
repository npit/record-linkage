import subprocess,os, datetime, argparse
from os.path import join


def write_config(config, filepath):
    with open(filepath, "w") as f:
        for elem in config:
            f.write("%s = %s\n" % (elem, config[elem]))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--simulate",action="store_true",dest="simulate")
    parser.add_argument("--overwrite",action="store_true",dest="overwrite")
    args = parser.parse_args()

    results_folder = "results_thresh_0.1"
    base_config = "config"
    evaluation_file = join(results_folder, "evaluation.txt")
    timings_file = join(results_folder, "timings.txt")

    # dataset parameter files
    # files = ["multiling.conf", "ng20.conf"]
    # files = ["multiling.conf"]
    # files = ["multiling_mixgraph.conf"]
    # files = ["multiling_mixgraph.conf", "ng20_mixgraph.conf"]

    files = ["multiling.conf", "ng20.conf", "multiling_mixgraph.conf", "ng20_mixgraph.conf"]

    if not os.path.exists(results_folder):
        os.mkdir(results_folder)
    if not args.overwrite:
        if os.path.exists(evaluation_file):
            print("Evaluation file",evaluation_file,"already exists.")
            exit(1)
        if os.path.exists(timings_file):
            print("Timings file",timings_file,"already exists.")
            exit(1)

    # representations compatible as per the jedai framework
    # bow and tftdf-bow, unigrams to trigrams: restrict to unigrams and trigrams
    representations = ["bow_w%d" % i for i in [1,3]]
    representations += ["bow_tfidf_w%d" % i for i in [1,3]]
    # word ng graphs, from uni to trigrams: restrict to trigrams
    representations += ["ngg_w%d" % i for i in [3]]
    # character ng graphs, from bi to fourgrams: restrict to trigrams
    representations += ["ngg_c%d" % i for i in [3]]
    # compatible similarities per representation
    sims = {"bow":["cosine", "jaccard"], "ngg":["nvs"]}
    # clustering approaches
    clustering = ["ricochet"]
    # clustering thresholds
    # clust_thresholds = [str(x/10) for x in list(range(1,10))]
    clust_thresholds = [0.1]

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
    results = []

    clust_thresholds = list(map(str,clust_thresholds))

    for dset in datasets:
        files_gt = dset["files_gt"]
        topics_gt = dset["topics_gt"]
        results_dset_folder = join(results_folder, dset["results"])
        if not os.path.exists(results_dset_folder):
            os.mkdir(results_dset_folder)

        if "similarities" in dset:
            modes = ["similarities"]
            repr_list = ["ngg_loaded"]
        else:
            modes = ["texts", "entities"]
            repr_list = representations

        for read_mode in modes:
            config["read_order"] = files_gt
            config["input_path"] = dset[read_mode]
            config["read_mode"] = read_mode
            if read_mode == "similarities":
                config["sim_field"] = dset["sim_field"]

            for repr in repr_list:
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
                            raw_output_file = join(results_dset_folder, "raw_results." + config_id)
                            write_config(config, config_file)

                            # run!
                            cmd = ["./execute.sh", config_file]
                            print(" ".join(cmd))
                            timestart = datetime.datetime.now()
                            if not args.simulate:
                                with open(output_file,"w") as ofile:
                                    with open(raw_output_file,"w") as raw_ofile:
                                        subprocess.run(cmd, stdout = ofile, stderr = raw_ofile)
                            time_elapsed = (datetime.datetime.now() - timestart).seconds

                            # evaluate
                            eval_cmd = ["python3", "parse_clustering.py", topics_gt, output_file,"--name",config_id]
                            print(" ".join(eval_cmd))
                            if not args.simulate:
                                with open(evaluation_file,"a") as ofile:
                                    subprocess.run(eval_cmd, stdout = ofile)

                            # write timings per configuration
                            if not args.simulate:
                                with open(timings_file,"a") as ofile:
                                    ofile.write("%s,%d\n" % (config_id,time_elapsed))

if __name__ == "__main__":
    main()
