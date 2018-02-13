import argparse
import numpy as np

parser = argparse.ArgumentParser()
parser.add_argument("res")
parser.add_argument("--exclude",nargs="*")
parser.add_argument("--only",nargs="*")
parser.add_argument("--verbose",action="store_true")
args = parser.parse_args()


metrics =[(2, "micro-F1"),
          ( 5 , "macro-F1"),
          ( 6 , "accuracy"),
          ( 7 , "cpr"),
          ( 8 , "pcpr")]

exclude = []
if args.exclude:
    exclude.extend(args.exclude)
only_include = []
if args.only:
    only_include.extend(args.only)
# only_include = ["jaccard"]

mean_perfs={}
mean_ranking={}
for metric in metrics:
  metric_index = metric[0]
  metric_name = metric[1]
  mean_perfs[metric_name] = {}
  mean_ranking[metric_name] = {}
  perconfig = {}
  perthresh = {}
  print("Metric:", metric_name)
  with open(args.res) as f:
      for line in f:
          line = line.strip()
          if exclude:
            if any([t in line for t in exclude]):continue
          if only_include:
              if not all([t in line for t in only_include]): continue
          parts = line.split()
          name, values = parts[0].split("."), parts[1:]
          if args.verbose:
              print(line)
              print(name)
          (dset, _, readmode, representation, sim, clust), thresh = name[:6],name[6:]
          thresh = float(".".join(thresh))
          val = values[metric_index]
          if thresh not in perthresh:
              perthresh[thresh] = []
          perthresh[thresh].append(float(val))


          id = "_".join([dset, readmode, representation, sim, clust])
          if id not in perconfig:
              perconfig[id] = {}
          perconfig[id][thresh] = val

  # mean performance per thresh
  print("Global mean performance per threshold:")
  for thresh in sorted([t for t in perthresh]):
      print("Threshold:",thresh,"mean performance:",np.mean(perthresh[thresh]))
      if thresh not in mean_perfs[metric_name]:
          mean_perfs[metric_name][thresh] = []
      mean_perfs[metric_name][thresh].append(np.mean(perthresh[thresh]))

  # mean performance per configuration:
  print("Mean performance per configuration:")
  threshold_ranks = {}
  k = 2
  for id in sorted([p for p in perconfig]):
      keyvals = perconfig[id].items()
      keyvals = sorted(keyvals, key = lambda x : x[1], reverse = True)
      print("Configuration:",id,"%d best performing threshold(s):" % k,keyvals[0:k])

      for i,(key,value) in enumerate(keyvals):
          if key not in threshold_ranks:
              threshold_ranks[key] = []
          threshold_ranks[key].append(i)

  print("Mean/majority threshold ranking:")
  for key in sorted([t for t in threshold_ranks]):
      positions = threshold_ranks[key]
      if args.verbose:
        print("thresh",key,"pos:",positions)
      counts = [(x, positions.count(x)) for x in set(positions)]
      _, maj_count = max(counts, key = lambda x : x[1])
      majority_positions = [x for x in counts if x[1] == maj_count]
      print("Threshold",key,"mean position:",np.mean(threshold_ranks[key]),"majority position(s):",majority_positions)
      if key not in mean_ranking[metric_name]:
          mean_ranking[metric_name][key] = []
      mean_ranking[metric_name][key].append(majority_positions[0])

print("Accross all evaluation methods:",metrics)
all_thresh = [t for n in mean_perfs for t in mean_perfs[n]]
all_thresh += [t for n in mean_ranking for t in mean_perfs[n]]
all_thresh = sorted(list(set(all_thresh)))
for t in all_thresh:
    perf = np.mean([mean_perfs[n][t] for n in mean_perfs])
    rank = np.mean([mean_ranking[n][t] for n in mean_ranking])
    print("Threshold:",t,"mean performance:",perf,"mean ranking:",rank)
