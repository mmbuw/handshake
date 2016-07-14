import sys
from math import factorial
from scipy.fftpack import fft, rfft, ifft
from os import listdir, path
from os.path import isfile, isdir, join, splitext
import matplotlib.pyplot as plt
import numpy as np
from scipy import spatial
from numpy import linalg as la

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
			meta_start = int(meta_data[2]) + 1
			meta_stop = meta_start+70 #int(meta_data[3]) + 1
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


	fft_values_complex = fft(values)
	fft_values = abs(fft(fft_values_complex)).real
	fft_values = fft_values[:len(fft_values)/2]
	fft_values_cut = abs(fft(values[start:stop])).real
	fft_values_cut = fft_values_cut[:len(fft_values_cut)/2]

	print values[0] - 1
	smooth_values = savitzky_golay(np.array(values), 11, 5)

	title = get_title_from_path(file)

	fig_title = "%s start: %s stop: %s" % (title, start, stop)
	fig = plt.figure()
	fig.suptitle(fig_title)

	plt.title(title)

	plt.subplot(4,1,1)
	plt.grid(True)
	plt.axvline(start, color="red")
	plt.axvline(stop, color="red")
	plt.plot(values)
	plt.plot(smooth_values)
	plt.ylim([-30,30])
	plt.xlim([0,500])
	
	plt.subplot(4,1,2)
	plt.grid(True)
	plt.plot(fft_values)
	plt.ylim([0,2000])
	plt.xlim([0,250])
	
	plt.subplot(4,1,3)
	plt.grid(True)
	plt.ylim([0,1000])
	plt.xlim([0,40])
	plt.plot(fft_values_cut)

	fft_values_cut = fft_values_cut[0:10]
	fft_values_cut = fft_values_cut / la.norm(fft_values_cut)

	plt.subplot(4,1,4)
	plt.grid(True)
	plt.ylim([0,1])
	plt.xlim([0,10])
	plt.plot(fft_values_cut)

	plt.tight_layout()

	if show_plot:
		plt.show()

	save_file = splitext(file)[0]+".png"
	plt.savefig(save_file, dpi = 160)
	plt.close()

def get_title_from_path(path):
	return path.split('/')[-1].split('.')[0]

def savitzky_golay(y, window_size, order, deriv=0, rate=1):
	try:
		window_size = np.abs(np.int(window_size))
		order = np.abs(np.int(order))
	except ValueError, msg:
		raise ValueError("window_size and order have to be of type int")
	if window_size % 2 != 1 or window_size < 1:
		raise TypeError("window_size size must be a positive odd number")
	if window_size < order + 2:
		raise TypeError("window_size is too small for the polynomials order")
	order_range = range(order+1)
	half_window = (window_size -1) // 2
	# precompute coefficients
	b = np.mat([[k**i for i in order_range] for k in range(-half_window, half_window+1)])
	m = np.linalg.pinv(b).A[deriv] * rate**deriv * factorial(deriv)
	# pad the signal at the extremes with
	# values taken from the signal itself
	firstvals = y[0] - np.abs( y[1:half_window+1][::-1] - y[0] )
	lastvals = y[-1] + np.abs(y[-half_window-1:-1][::-1] - y[-1])
	y = np.concatenate((firstvals, y, lastvals))
	return np.convolve( m[::-1], y, mode='valid')


if __name__ == '__main__':
	file = sys.argv[1]
	if isfile(file):
		plot(file)
	if isdir(file):
		plot_all_files_in_dir(file)
	else:
		print "Wrong input."

