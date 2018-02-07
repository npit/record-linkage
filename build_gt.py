import argparse
from os.path import join
import os
"""
Construct the ground truth for each dataset
"""

def parse_multiling(infile):
    print("Parsing multiling")
    # multiling msms source texts
    # set wether ground truth:
    # should span multiple languages: "multilingual"
    # should be language-specific: "separate" 
    multilinguality_setting = "multilingual"
    desired_langs = ["english", "french", "spanish"]
    files_per_topic = 10
    topic_ids = []
    # keep track
    files_mapping = {}
    topics_to_files = {}
    files_to_topics = {}


    for lang in os.listdir(infile):
        if lang not in desired_langs:
            continue
        langfiles = sorted(os.listdir(join(infile,lang)))
        print("Parsing language",lang, "files:",len(langfiles))
        divisions = range(0,  len(langfiles), files_per_topic)
        langfiles = [langfiles[i:i+files_per_topic] for i in divisions]
        lang_topics = list(range(len(langfiles)))

        # shared topics across languages
        if multilinguality_setting == "multilingual":
            pass

        # language-specific topics
        if multilinguality_setting == "separate":
            lang_topics = ["%s_%d" % (lang, i) for i in lang_topics]
        for t in lang_topics:
            if t not in topics_to_files:
                topics_to_files[t] = []

        for topic_idx, topic in enumerate(lang_topics):
            for f in langfiles[topic_idx]:
                file_index = len(files_mapping)
                files_mapping[join(lang, f)] = file_index
                files_to_topics[file_index] = topic
                topics_to_files[topic].append(files_mapping[join(lang, f)])

    # write file to file index
    with open("multiling_gt_files2idxs.txt","w") as f:
        for file in files_mapping:
            f.write("%s %d\n" % (file, files_mapping[file]))
    # write gt per topic
    with open("multiling_gt_topics2files.txt","w") as f:
        for topic in topics_to_files:
            f.write("%d %s\n" % (topic, " ".join([str(x) for x in topics_to_files[topic]])))
    # write gt per file index
    with open("multiling_gt_files2topics.txt","w") as f:
        for fileidx in files_to_topics:
            f.write("%d %d\n" % (fileidx, files_to_topics[fileidx]))


def parse_newsgroups(infile):
    print("Parsing 20 newsgroups:")
    # 20 newsgroups sources
    # set wether ground truth:
    # should span multiple languages: "multilingual"
    # should be language-specific: "separate"
    files_mapping = {}
    topics_mapping = {}
    topics_to_files = {}
    files_to_topics = {}


    for topic_idx, topic in enumerate(os.listdir(infile)):
        topics_mapping[topic] = len(topics_mapping)
        topics_to_files[topic_idx] = []
        topicfiles = sorted(os.listdir(join(infile,topic)))
        print("Parsing topic:", topic,"with %d files" % len(topicfiles))


        for i, file in enumerate(topicfiles):
            file_index = len(files_mapping)
            files_mapping[join(topic,file)] = file_index
            files_to_topics[file_index] = topic_idx
            topics_to_files[topic_idx].append(file_index)

    # write topic to topic index
    with open("20newsgroups_gt_topics2idxs.txt","w") as f:
        for topic in topics_mapping:
            f.write("%s %d\n" % (topic, topics_mapping[topic]))
    # write file to file index
    with open("20newsgroups_gt_files2idxs.txt","w") as f:
        for file in files_mapping:
            f.write("%s %d\n" % (file, files_mapping[file]))
    # write gt per topic
    with open("20newsgroups_gt_topics2files.txt","w") as f:
        for topic in topics_to_files:
            topic_idx = topics_to_files[topic]
            f.write("%s %s\n" % (topic_idx, " ".join([str(x) for x in topics_to_files[topic]])))
    # write gt per file index
    with open("20newsgroups_gt_files2topics.txt","w") as f:
        for fileidx in files_to_topics:
            f.write("%d %d\n" % (fileidx, files_to_topics[fileidx]))




if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--multiling", dest="mling")
    parser.add_argument("--20news", dest="news")
    args = parser.parse_args()
    if not (args.mling or args.news):
        print("Nothing to do.")
        exit(1)

    if args.mling:
        parse_multiling(args.mling)
    if args.news:
        parse_newsgroups(args.news)
