import sys
import itertools
import argparse
#from sklearn.metrics import precision_recall_curve
#from sklearn.metrics import average_precision_score


"""
Parse clustering results
"""

def get_cpr():
    """
    Compute the clustering precision measure, from (Hassanzadeh  2009)
    """
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("gt", help = "Ground truth. Space-delimited file indexes per line.")
    parser.add_argument("res", help = "Clustering results. Space-delimited file indexes per line.")
    parser.add_argument("--name")
    parser.add_argument("--verbose", action="store_true", dest = "verbose")
    args = parser.parse_args()

    # read ground truth
    topics2files = {}
    files2topics = {}
    with open(args.gt) as f:
        for line in f:
            line = line.strip().split()
            topic, files = line[0], line[1:]
            if topic not in topics2files:
                topics2files[topic] = []
            topics2files[topic].extend(files)
            for f in files:
                if f in files2topics:
                    print("File %s already in file gt!" % f)
                    exit(1)
                files2topics[f] = topic

    # store a dict of predicted topic clusters with members
    predicted = {}
    # read results file
    with open(args.res) as f:
        for i, line in enumerate(f):
            # read the file indexes
            idxs = line.strip().split()
            # map file indexes to their ground truth topics indexes
            topics = [files2topics[f] for f in idxs]
            # cluster topic is the most common in the cluster. if a single most common does not exist, discard cluster
            # count'em
            counts = [(x,topics.count(x)) for x in set(topics)]
            # skip if there is no most frequent topic
            if all([x == counts[0][1] for x in counts]):
                print("Cluster discarded:",topics)
                continue
            # else, set the most frequent to be the cluster topic
            cluster_topic = max(counts, key = lambda x : x[1])[0]
            if args.verbose:
                print("Cluster #%d" % (i+1), "topic:",cluster_topic,"from topics:",topics)
            # assign files to cluster
            if cluster_topic not in predicted:
                predicted[cluster_topic] = []
            predicted[cluster_topic].extend(idxs)


    if args.verbose:
        print("Unmerged file assignments:")
        for topic in predicted:
            print("Clust.topic:",topic,", member topics:",predicted[topic])

    # drop duplicate file occurences in a cluster, and map to topic indexes
    for topic in predicted:
        unique_fidxs = list(set(predicted[topic]))
        predicted[topic] = [files2topics[f] for f in unique_fidxs]

    if args.verbose:
        print("Merged and mapped file assignments:")
        for topic in predicted:
            print("Clust.topic:",topic,", member topics:",predicted[topic])

    # compute precision, recall and f-score
    precs, recs, fscores = [], [], []
    for ptopic in predicted:
        # prec, rec, f
        pred_files = predicted[ptopic]
        gt_files = topics2files[ptopic]
        true_pos = [x for x in pred_files if x in gt_files]
        prec = len(true_pos) / len(pred_files)
        rec = len(true_pos) / len(gt_files)
        if true_pos:
            fscore = 2 * (prec * rec) / (prec + rec)
        else:
            fscore = 0.0
        precs.append(prec)
        recs.append(rec)
        fscores.append(fscore)

    # mean prec, rec, f values
    means = [sum(precs)/len(precs), \
         sum(recs)/len(recs), \
         sum(fscores) / len(fscores)]


    # cpr measure: examine the topics per cluster
    marginal_cprs = []
    num_cprs = 0
    for (predtop, topics) in predicted.items():
        if len(topics) == 1:
            continue
        num_cprs +=1
        # percentage of pairs that belong to same cluster
        combos = list(itertools.combinations(topics, 2))
        combos.sort()
        sametopic_combos = [1 if x[0] == x[1] else 0 for x in combos]
        clust_cpr = sum(sametopic_combos) / len(combos)
        if args.verbose:
            print("cpr for cluster topic",predtop,":",clust_cpr)
        marginal_cprs.append(clust_cpr)

    if args.verbose:
        print("marginals:",len(marginal_cprs))
    cpr = sum(marginal_cprs)/len(marginal_cprs)
    num_clusters_pred_gt = [len(predicted), len(topics2files)]
    # for pcpr, get the ratio of missing or extra clusters
    pcpr_coeff = min(num_clusters_pred_gt) / max(num_clusters_pred_gt)
    if args.verbose:
        print("pcpr coefficient: ",pcpr_coeff)
    pcpr = cpr * pcpr_coeff
    means += [cpr, pcpr]

    name = args.name if args.name else "test_run"
    print(name,*means)

