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
    parser.add_argument("--verbose", dest = "verbose")
    args = parser.parse_args()

    # read ground truth
    topics2files = {}
    files2topics = {}
    with open(args.gt) as fpr:
        for line in fpr:
            line = line.strip().split()
            topic, files = line[0], line[1:]
            if topic not in topics2files:
                topics2files[topic] = []
            topics2files[topic].extend(files)
            for f in files:
                assert f not in files2topics, "File %s already in file gt!" % f
                files2topics[f] = topic

    # config name
    name = args.name if args.name else "test_run"

    # store a dict of predicted topic clusters with members
    predicted = {}
    # read results file
    with open(args.res) as fpr:
        for i, line in enumerate(fpr):
            # read the file indexes
            idxs = line.strip().split()
            # map file indexes to their ground truth topics indexes
            cluster_topics = [files2topics[f] for f in idxs]
            # cluster topic is the most common in the cluster. if a single most common does not exist, discard cluster
            # count'em
            counts = [(x,cluster_topics.count(x)) for x in sorted(list(set(cluster_topics)))]
            # skip if there is no most frequent topic
            if all([x == counts[0][1] for x in counts]):
                print("Cluster discarded:",cluster_topics)
                continue
            # else, set the most frequent to be the cluster topic
            majority_topic = max(counts, key = lambda x : x[1])[0]
            if args.verbose:
                print("Cluster #%d" % (i+1), "topic:",majority_topic,"from topics:",cluster_topics)
            # assign files to cluster
            if majority_topic not in predicted:
                predicted[majority_topic] = []
            predicted[majority_topic].extend(idxs)


    sorted_pred = list(map(str,sorted([int(x) for x in predicted])))
    sorted_top = list(map(str,sorted([int(x) for x in topics2files])))

    if args.verbose:
        print("Unmerged file assignments:")
        for topic in sorted_pred:
            print("Clust.topic:",topic,", member topics:",predicted[topic])

    # drop duplicate file occurences in a cluster, and map to topic indexes
    for topic in sorted_pred:
        unique_fidxs = list(set(predicted[topic]))
        predicted[topic] = [files2topics[f] for f in unique_fidxs]

    if args.verbose:
        print("Merged and mapped file assignments:")
        for topic in sorted_pred:
            print("Clust.topic:",topic,", member topics:",predicted[topic])

    # compute evaluations
    header = "mi-precision mi-recall mi-f1 ma-precision ma-recall ma-f1 cpr pcpr"
    if not predicted:
        if args.verbose:
            print(header)
        print(name," ".join(["0.0" for _ in range(len(header.split()))]))
        exit(0)
    if args.verbose:
        print("Computing evaluation metrics.")

    # compute precision, recall and f-score
    precs, recs, fscores = [], [], []
    num_macro_tp, num_macro_gt, num_macro_pred = 0, 0, 0
    for ptopic in sorted_top:
        # prec, rec, f
        if ptopic in predicted:
            pred_topics = predicted[ptopic]
            num_true_pos = pred_topics.count(ptopic)
            prec = num_true_pos / len(pred_topics)
        else:
            pred_topics = []
            num_true_pos = 0
            prec = 0

        num_gt = len(topics2files[ptopic])
        rec = num_true_pos / num_gt
        if num_true_pos:
            fscore = 2 * (prec * rec) / (prec + rec)
        else:
            fscore = 0.0

        if args.verbose:
            print("Topic:",ptopic,"num tp, p, gtp" ,num_true_pos,len(pred_topics),num_gt,"prec, rec, f",prec,rec,fscore)
        num_macro_tp += num_true_pos
        num_macro_gt += num_gt
        num_macro_pred += len(pred_topics)

        precs.append(prec)
        recs.append(rec)
        fscores.append(fscore)

    # macro prec, rec, f values
    macro_scores = [sum(precs)/len(precs), \
         sum(recs)/len(recs), \
         sum(fscores) / len(fscores)]
    # micro prec, rec, f values
    micro_scores = [num_macro_tp / num_macro_pred, \
                    num_macro_tp / num_macro_gt ]
    micro_scores += [ 2 * micro_scores[0] * micro_scores[1] / (micro_scores[0] + micro_scores[1]) ]
    scores = micro_scores + macro_scores


    if args.verbose:
        print("Macro scores:", macro_scores,"| micro scores:", micro_scores)

    # cpr measure: examine the topics per cluster
    marginal_cprs = []
    num_cprs = 0
    for ptopic in sorted_pred:
        topics = predicted[ptopic]
        if len(topics) == 1:
            continue
        num_cprs +=1
        # percentage of pairs that belong to same cluster
        combos = list(itertools.combinations(topics, 2))
        combos.sort()
        sametopic_combos = [1 if x[0] == x[1] else 0 for x in combos]
        clust_cpr = sum(sametopic_combos) / len(combos)
        if args.verbose:
            print("cpr for cluster topic",ptopic,":",clust_cpr)
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
    scores += [cpr, pcpr]

    if args.verbose:
        print(header)
    print(name,*scores)

