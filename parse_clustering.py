import sys
import argparse
#from sklearn.metrics import precision_recall_curve
#from sklearn.metrics import average_precision_score


"""
Parse clustering results
"""

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("gt", help = "Ground truth. Space-delimited file indexes per line.")
    parser.add_argument("res", help = "Clustering results. Space-delimited file indexes per line.")
    parser.add_argument("--name")
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
                    print("File %d already in file gt!" % f)
                    exit(1)
                files2topics[f] = topic

    predicted = {}
    # read results file
    with open(args.res) as f:
        for line in f:
            idxs = line.strip().split()
            # map file indexes to topics indexes
            topics = [files2topics[f] for f in idxs]
            # cluster topic : the most common. if not a single most common, discard
            counts = [(x,topics.count(x)) for x in set(topics)]
            # skip if there is no most frequent topic
            if all([x == counts[0][1] for x in counts]):
                continue
            cluster_topic = max(counts, key = lambda x : x[1])[0]
            if cluster_topic in predicted:
                # merge
                predicted[cluster_topic] = list(set(predicted[cluster_topic] + idxs))
            else:
                predicted[cluster_topic] = idxs

    # compute precision, recall and f-score
    precs, recs, fscores = [], [], []
    for ptopic in predicted:
        pred_files = predicted[ptopic]
        gt_files = topics2files[ptopic]
        true_pos = [x for x in pred_files if x in gt_files]
        prec = len(true_pos) / len(pred_files)
        rec = len(true_pos) / len(gt_files)
        fscore = 2 * (prec * rec) / (prec + rec)
        precs.append(prec)
        recs.append(rec)
        fscores.append(fscore)

    # mean values
    means = sum(precs)/len(precs), \
         sum(recs)/len(recs), \
         sum(fscores) / len(fscores)
    name = args.name if args.name else "test_run"
    print(name,*means)

