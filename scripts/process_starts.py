import sys
from scipy.fftpack import fft, rfft, ifft
from os import listdir, path
from os.path import isfile, isdir, join, splitext
import matplotlib.pyplot as plt
import numpy as np
from scipy.signal import argrelextrema
from scipy import spatial
from numpy import linalg as la

def process_all_files_in_dir(file_dir):
    differences = []
    txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
    for file in txtfiles:

        diff = process_file(file)

        if diff is not None:
            differences.append(diff)

    # difference statistics
    print ""
    print "Extracted differences statistics:"
    print "-----------------------------------------------"
    print "Mean: " + str(np.mean(differences))
    print "Median: " + str(np.median(differences))
    print "Maximum: " + str(np.max(differences))

    counts = np.bincount(differences)
    below_five = 0

    for i in range(5):
        below_five += counts[i]

    print "Perfect hits: " + str(counts[0]) + " of " + str(len(differences))
    print "Hits below five: " + str(below_five) + " of " + str(len(differences))

def process_file(file):
    values = []

    with open(file, 'r') as f:
        lines = f.readlines()

        if lines[0].startswith('#'):
            meta_data = lines[0].split(', ')
            devicename = meta_data[0]
            timestamp = meta_data[1]
            meta_start = int(meta_data[2])
            meta_stop = int(meta_data[3])
            labelled_start = int(meta_data[4])

            if labelled_start == 999:
                return 

            lines = lines[1:]

        for line in lines:
            value = line.split(', ')[1]
            values.append(float(value))

    if len(sys.argv) > 2:
        start = int(sys.argv[2])
        stop = int(sys.argv[3])
    else:
        start = meta_start
        stop = meta_stop

    title = get_title_from_path(file)

    # new start detection
    data = np.array(values)
    extracted_start = detect_start(data)

    print str(labelled_start) + " " + str(extracted_start) + " " + str(abs(labelled_start-extracted_start))

    if abs(labelled_start-extracted_start) > 50:
        plot(file)

    return abs(labelled_start-extracted_start)



def plot_all_files_in_dir(file_dir):
    txtfiles = [join(file_dir, f) for f in listdir(file_dir) if isfile(join(file_dir, f)) and f.endswith('.txt')]
    for file in txtfiles:
        plot(file, show_plot= False)


def plot(file, show_plot=True):
    values = []

    with open(file, 'r') as f:
        lines = f.readlines()

        if lines[0].startswith('#'):
            meta_data = lines[0].split(', ')
            devicename = meta_data[0]
            timestamp = meta_data[1]
            meta_start = int(meta_data[2])
            meta_stop = int(meta_data[3])
            labelled_start = int(meta_data[4])
            lines = lines[1:]

        for line in lines:
            value = line.split(', ')[1]
            values.append(float(value))

    if len(sys.argv) > 2:
        start = int(sys.argv[2])
        stop = int(sys.argv[3])
    else:
        start = meta_start
        stop = meta_stop

    title = get_title_from_path(file)

    # new start detection
    data = np.array(values)
    extracted_start = detect_start(data)

    print start
    print extracted_start

    # data plot
    fig_title = "%s start: %s stop: %s" % (title, start, stop)
    fig = plt.figure()
    fig.suptitle(fig_title)

    plt.subplot(1,1,1)
    plt.grid(True)
    plt.axvline(start, color="red")
    plt.axvline(stop, color="red")
    plt.axvline(extracted_start, color="black")
    plt.axvline()
    plt.plot(values)
    plt.ylim([-30,30])
    plt.xlim([0,500])

    plt.tight_layout()

    if show_plot:
        plt.show()

    #save_file = splitext(file)[0]+".png"
    #plt.savefig(save_file, dpi = 160)
    plt.close()


def detect_start(data):

    maxima = argrelextrema(data = data, comparator = np.greater_equal, order = 3)
    minima = argrelextrema(data = data, comparator = np.less_equal, order = 3)
    extremum_list = create_extremum_list(maxima, minima)

    global_maximum = np.amax(data)
    global_minimum = np.amin(data)
    #upper_thresh = 0.6 * global_maximum
    #lower_thresh = 0.6 * global_minimum
    
    for i in range(len(extremum_list)-1):
        current_tuple = extremum_list[i]
        current_position = current_tuple[0]
        current_type = current_tuple[1]

        if current_type == 0:
            continue

        current_value = data[current_position]

        #if current_value < lower_thresh:

        next_tuple = extremum_list[i+1]
        next_position = next_tuple[0]
        next_type = next_tuple[1]
        next_value = data[next_position]

        #if next_type == 0 and next_value > upper_thresh:
        if next_type == 0 and abs(next_value-current_value) > 0.7 * (global_maximum-global_minimum):
            return current_position

    return -1



def create_extremum_list(maxima_list, minima_list):

    zipped_list = []

    for i in range(len(maxima_list[0])):
        zipped_list.append((maxima_list[0].item(i), 0))

    for i in range(len(minima_list[0])):
        zipped_list.append((minima_list[0].item(i), 1))

    zipped_list = sorted(zipped_list, key=lambda tup: tup[0])
    return zipped_list

def get_title_from_path(path):
    return path.split('/')[-1].split('.')[0]

if __name__ == '__main__':
    file = sys.argv[1]
    if isfile(file):
        process_file(file)
    if isdir(file):
        process_all_files_in_dir(file)