import sys
import json
import matplotlib.pyplot as plt

if __name__ == '__main__':
	data_json = sys.argv[1]
	with open(data_json) as data_file:    
	    data = json.load(data_file)

	fft_y2 = [x["fft_y"][2] for x in data["65"].values()]

	print max(fft_y2)
	plt.hist(fft_y2, bins=int(sys.argv[2]))
	plt.show()
