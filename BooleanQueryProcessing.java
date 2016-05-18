package com.boolQuery;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class BooleanQueryProcessing {

	public static void main(String args[]) {
		BooleanQueryProcessing bpq = new BooleanQueryProcessing();
		ArrayList<DictionaryTerm> docIdSortedArray = bpq.createDocIdSortedIndex();
		ArrayList<DictionaryTerm> termFreqSortedArray = bpq.createTermFreqSortedIndex();

		bpq.getTopKTerms(docIdSortedArray, 5);

		String[] queryTerms = { "zinc", "America" };
		bpq.getPostings(queryTerms, docIdSortedArray, termFreqSortedArray);

		bpq.termAtATimeQueryAnd(queryTerms, termFreqSortedArray);

		bpq.termAtATimeQueryOr(queryTerms, termFreqSortedArray);

	}

	private ArrayList<DictionaryTerm> createDocIdSortedIndex() {
		ArrayList<DictionaryTerm> docIdSortedArray = new ArrayList<DictionaryTerm>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("D:/UB SEM 1/IR/Programming Assgn/term.idx"));
			String line;
			while ((line = in.readLine()) != null) {
				LinkedList<Index> postingsList = new LinkedList<Index>();
				String[] indexElements = line.split("\\\\");

				// Postings list
				String pList = indexElements[2];
				pList = pList.substring(2, pList.length() - 1);
				String[] indexList = pList.split(", ");
				for (String iList : indexList) {
					String[] docInfoString = iList.split("/");
					Index index = new Index(Integer.parseInt(docInfoString[0]), Integer.parseInt(docInfoString[1]));
					postingsList.add(index);
				}
				// Sort the postings list on docId
				Collections.sort(postingsList, new Comparator<Index>() {
					@Override
					public int compare(Index o1, Index o2) {
						return (o1.getDocId()).compareTo(o2.getDocId());
					}
				});
				DictionaryTerm dict1 = new DictionaryTerm(indexElements[0], Integer.parseInt(indexElements[1].substring(1)), postingsList);
				docIdSortedArray.add(dict1);
			}
			in.close();
		} catch (IOException e) {
		}
		return docIdSortedArray;
	}

	private ArrayList<DictionaryTerm> createTermFreqSortedIndex() {
		ArrayList<DictionaryTerm> termFreqSortedArray = new ArrayList<DictionaryTerm>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("D:/UB SEM 1/IR/Programming Assgn/term.idx"));
			String line;
			while ((line = in.readLine()) != null) {
				LinkedList<Index> postingsList = new LinkedList<Index>();
				String[] indexElements = line.split("\\\\");

				// Postings list
				String pList = indexElements[2];
				pList = pList.substring(2, pList.length() - 1);
				String[] indexList = pList.split(", ");
				for (String iList : indexList) {
					String[] docInfoString = iList.split("/");
					Index index = new Index(Integer.parseInt(docInfoString[0]), Integer.parseInt(docInfoString[1]));
					postingsList.add(index);
				}
				// Sort the postings list on term freq
				Collections.sort(postingsList, new Comparator<Index>() {
					@Override
					public int compare(Index o1, Index o2) {
						return (o2.getTermFreq()).compareTo(o1.getTermFreq());
					}
				});
				DictionaryTerm dict2 = new DictionaryTerm(indexElements[0], Integer.parseInt(indexElements[1].substring(1)), postingsList);
				termFreqSortedArray.add(dict2);
			}
			in.close();
		} catch (IOException e) {
		}
		return termFreqSortedArray;
	}

	private void getTopKTerms(ArrayList<DictionaryTerm> indexList, int k) {
		// Sort the list on size
		Collections.sort(indexList, new Comparator<DictionaryTerm>() {
			@Override
			public int compare(DictionaryTerm o1, DictionaryTerm o2) {
				return (o1.getpLSize()).compareTo(o2.getpLSize());
			}
		});
		System.out.println("FUNCTION: getTopKTerms " + k);
		System.out.print("RESULT: ");
		for (int i = 0; i < k; i++) {
			DictionaryTerm dictTerm = indexList.get(indexList.size() - i - 1);
			System.out.print(dictTerm.getTerm() + ", ");
		}
	}

	private void getPostings(String[] queryTerms, ArrayList<DictionaryTerm> docIdSortedArray, ArrayList<DictionaryTerm> termFreqSortedArray) {

		for (String qT : queryTerms) {
			boolean termfound = false;
			for (DictionaryTerm dictTerm : docIdSortedArray) {
				if (dictTerm.getTerm().equals(qT)) {
					System.out.println("");
					System.out.println("");
					System.out.println("FUNCTION: getPostings " + qT);
					System.out.print("Ordered by doc IDs: ");
					for (Index index : dictTerm.getIndexList())
						System.out.print(index.getDocId() + ", ");
					termfound = true;
					break;
				}
			}
			if (termfound == true) {
				for (DictionaryTerm dictTerm : termFreqSortedArray) {
					if (dictTerm.getTerm().equals(qT)) {
						System.out.println("");
						System.out.print("Ordered by Term Freqs: ");
						for (Index index : dictTerm.getIndexList())
							System.out.print(index.getDocId() + ", ");
						break;
					}
				}
			}
			if (termfound == false) {
				System.out.println("");
				System.out.println("");
				System.out.println("FUNCTION: getPostings " + qT);
				System.out.println("Term Not Found");
			}
		}
	}

	private void termAtATimeQueryAnd(String[] queryTerms, ArrayList<DictionaryTerm> termFreqSortedArray) {

		long startTime = System.currentTimeMillis();

		ArrayList<LinkedList<Index>> postingsListArray = new ArrayList<LinkedList<Index>>();
		LinkedList<Integer> tempResult = new LinkedList<Integer>();
		ArrayList<DictionaryTerm> compDT = new ArrayList<DictionaryTerm>();
		int count = 1, noOfComparison = 0, noOfComparisonOptimized = 0;
		boolean termFound = false;

		// Create Temp Result and postings list for comparison
		for (String qT : queryTerms) {
			for (DictionaryTerm dictTerm : termFreqSortedArray) {
				if (dictTerm.getTerm().equals(qT))
					compDT.add(dictTerm);
			}
		}

		// Create Temp Result by adding first dict term and PL array by rest
		for (DictionaryTerm dictTerm : compDT) {
			if (count == 1) {
				// System.out.println(" Term is : "+dictTerm.getTerm());
				// Creates Temp Result List
				// System.out.println(" ");
				// System.out.println("Temp Result values");
				for (Index idx : dictTerm.getIndexList()) {
					tempResult.add(idx.getDocId());
					// System.out.println(idx.getDocId() + " ");
				}
				count++;
			} else {
				postingsListArray.add(dictTerm.getIndexList());
				// System.out.println("Postings List added to array"
				// +dictTerm.getTerm());
			}
		}

		// Compare temp result and postings lists
		for (LinkedList<Index> postingsList : postingsListArray) {

			for (Iterator<Integer> it = tempResult.iterator(); it.hasNext();) {
				termFound = false;
				Integer docId = it.next();
				for (Index idx : postingsList) {
					// System.out.println(docId + " vs " + idx.docId);
					noOfComparison++;
					if (docId.equals(idx.docId)) {
						// System.out.println("Term Found in DocId : " + docId);
						termFound = true;
						break;
					}
				}
				if (termFound == false) {
					it.remove();
					// System.out.println(" " + docId + " Removed.. New Size : "
					// + tempResult.size());
				}
			}
		}

		// Comparison optimization
		Collections.sort(compDT, new Comparator<DictionaryTerm>() {
			@Override
			public int compare(DictionaryTerm o1, DictionaryTerm o2) {
				return (o1.getpLSize()).compareTo(o2.getpLSize());
			}
		});
		count = 1;
		tempResult.clear();
		postingsListArray.clear();

		for (DictionaryTerm dictTerm : compDT) {
			if (count == 1) {
				// System.out.println(" Term is : "+dictTerm.getTerm());
				// Creates Temp Result List
				// System.out.println(" ");
				// System.out.println("Temp Result values");
				for (Index idx : dictTerm.getIndexList()) {
					tempResult.add(idx.getDocId());
					// System.out.println(idx.getDocId() + " ");
				}
				count++;
			} else {
				postingsListArray.add(dictTerm.getIndexList());
				// System.out.println("Postings List added to array"
				// +dictTerm.getTerm());
			}
		}

		// Compare temp result and postings lists
		for (LinkedList<Index> postingsList : postingsListArray) {

			for (Iterator<Integer> it = tempResult.iterator(); it.hasNext();) {
				termFound = false;
				Integer docId = it.next();
				for (Index idx : postingsList) {
					// System.out.println(docId + " vs " + idx.docId);
					noOfComparisonOptimized++;
					if (docId.equals(idx.docId)) {
						// System.out.println("Term Found in DocId : " + docId);
						termFound = true;
						break;
					}
				}
				if (termFound == false) {
					it.remove();
					// System.out.println(" " + docId + " Removed.. New Size : "
					// + tempResult.size());
				}
			}
		}

		StringBuffer queryBuffer = new StringBuffer();
		for (String qT : queryTerms) {
			queryBuffer.append(qT);
			queryBuffer.append(" ");
		}
		System.out.println("");
		System.out.println("");
		System.out.println("FUNCTION: termAtATimeQueryAnd " + queryBuffer);
		System.out.println(tempResult.size() + " documents are found");
		System.out.println(noOfComparison + " comparisons are made");
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 100.00 + " seconds are used");
		System.out.println(noOfComparisonOptimized + " comparisons are made with optimization");
		if (tempResult.isEmpty()) {
			System.out.println("Term Not Found");
		} else {
			System.out.print("RESULT :");
			Collections.sort(tempResult, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
			});
			for (Integer docId : tempResult) {
				System.out.print(docId + ", ");
			}
		}
	}

	private void termAtATimeQueryOr(String[] queryTerms, ArrayList<DictionaryTerm> termFreqSortedArray) {
		long startTime = System.currentTimeMillis();

		ArrayList<LinkedList<Index>> postingsListArray = new ArrayList<LinkedList<Index>>();
		LinkedList<Integer> tempResult = new LinkedList<Integer>();
		ArrayList<DictionaryTerm> compDT = new ArrayList<DictionaryTerm>();
		int count = 1, noOfComparison = 0, noOfComparisonOptimized = 0;
		boolean termFound = false;

		// Create List of Dictionary Term from query Term array
		for (String qT : queryTerms) {
			for (DictionaryTerm dictTerm : termFreqSortedArray) {
				if (dictTerm.getTerm().equals(qT))
					compDT.add(dictTerm);
			}
		}

		// Create Temp Result by adding first dict term and PL array by rest
		for (DictionaryTerm dictTerm : compDT) {
			if (count == 1) {
				// System.out.println(" Term is : "+dictTerm.getTerm());
				// Creates Temp Result List
				// System.out.println(" ");
				// System.out.println("Temp Result values");
				for (Index idx : dictTerm.getIndexList()) {
					tempResult.add(idx.getDocId());
					// System.out.println(idx.getDocId() + " ");
				}
				count++;
			} else {
				postingsListArray.add(dictTerm.getIndexList());
				// System.out.println("Postings List added to array"
				// +dictTerm.getTerm());
			}
		}

		// Compare temp result and postings lists
		for (LinkedList<Index> postingsList : postingsListArray) {

			for (Index idx : postingsList) {
				termFound = false;
				for (Integer docId : tempResult) {
					// System.out.println(docId + " vs " + idx.docId);
					noOfComparison++;
					if (docId.equals(idx.docId)) {
						// System.out.println("Term Found in DocId : " + docId);
						termFound = true;
						break;
					}
				}
				if (termFound == false) {
					tempResult.add(idx.docId);
					// System.out.println(" " + idx.docId +
					// " Added.. New Size : " + tempResult.size());
				}
			}

			// Comparison optimization
			Collections.sort(compDT, new Comparator<DictionaryTerm>() {
				@Override
				public int compare(DictionaryTerm o1, DictionaryTerm o2) {
					return (o2.getpLSize()).compareTo(o1.getpLSize());
				}
			});
			count = 1;
			tempResult.clear();
			postingsListArray.clear();

			// Create Temp Result by adding first dict term and PL array by rest
			for (DictionaryTerm dictTerm : compDT) {
				if (count == 1) {
					// System.out.println(" Term is : "+dictTerm.getTerm());
					// Creates Temp Result List
					// System.out.println(" ");
					// System.out.println("Temp Result values");
					for (Index idx : dictTerm.getIndexList()) {
						tempResult.add(idx.getDocId());
						// System.out.println(idx.getDocId() + " ");
					}
					count++;
				} else {
					postingsListArray.add(dictTerm.getIndexList());
					// System.out.println("Postings List added to array"
					// +dictTerm.getTerm());
				}
			}

			// Compare temp result and postings lists
			for (LinkedList<Index> postingsList1 : postingsListArray) {
				for (Index idx : postingsList1) {
					termFound = false;
					for (Integer docId : tempResult) {
						// System.out.println(docId + " vs " + idx.docId);
						noOfComparisonOptimized++;
						if (docId.equals(idx.docId)) {
							// System.out.println("Term Found in DocId : " +
							// docId);
							termFound = true;
							break;
						}
					}
					if (termFound == false) {
						tempResult.add(idx.docId);
						// System.out.println(" " + idx.docId +
						// " Added.. New Size : " + tempResult.size());
					}
				}
			}
		}

		StringBuffer queryBuffer = new StringBuffer();
		for (String qT : queryTerms) {
			queryBuffer.append(qT);
			queryBuffer.append(" ");
		}
		System.out.println("");
		System.out.println("");
		System.out.println("FUNCTION: termAtATimeQueryOr " + queryBuffer);
		System.out.println(tempResult.size() + " documents are found");
		System.out.println(noOfComparison + " comparisons are made");
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) / 100.00 + " seconds are used");
		System.out.println(noOfComparisonOptimized + " comparisons are made with optimization");
		if (tempResult.isEmpty()) {
			System.out.println("Term Not Found");
		} else {
			System.out.print("RESULT :");
			Collections.sort(tempResult, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
			});
			for (Integer docId : tempResult) {
				System.out.print(docId + ", ");
			}
		}
	}

	private void docAtATimeQueryAnd(String[] queryTerms, ArrayList<DictionaryTerm> docIdSortedArray) {
		long startTime = System.currentTimeMillis();

		ArrayList<LinkedList<Index>> postingsListArray = new ArrayList<LinkedList<Index>>();
		ArrayList<ListIterator<Index>> iteratorArray = new ArrayList<ListIterator<Index>>();
		int count = 1, noOfComparison = 0, noOfComparisonOptimized = 0;

		// Create Postings List Array
		for (String qT : queryTerms) {
			for (DictionaryTerm dictTerm : docIdSortedArray) {
				if (dictTerm.getTerm().equals(qT)) {
					ListIterator<Index> postingsListIterator = dictTerm.getIndexList().listIterator();
					iteratorArray.add(postingsListIterator);
					postingsListArray.add(dictTerm.getIndexList());
					System.out.println("Postings List added to array" + dictTerm.getTerm());
				}
			}
		}

		if (postingsListArray.size() == iteratorArray.size()) {
			System.out.println("Iterators created successfully");
		}

		LinkedList<Index> postingsList = postingsListArray.get(0);
		ArrayList<Integer> intersectedList = new ArrayList<Integer>();
		for (int i = 0, j = 0; i < postingsList.size() - 1; i++, j++) {
			if (postingsList.get(i).getDocId().equals(postingsList.get(j).getDocId())) {
				intersectedList.add(postingsList.get(i).getDocId());
				i++;
				j++;
			} else {
				
			}

		}
	}

	private class Index {
		private Integer docId;
		private Integer termFreq;

		private Index(int docId, int termFreq) {
			this.setDocId(docId);
			this.setTermFreq(termFreq);
		}

		private Integer getDocId() {
			return docId;
		}

		private void setDocId(Integer docId) {
			this.docId = docId;
		}

		private Integer getTermFreq() {
			return termFreq;
		}

		private void setTermFreq(Integer termFreq) {
			this.termFreq = termFreq;
		}
	}

	private class DictionaryTerm {
		private String term;
		private Integer pLSize;
		private LinkedList<Index> indexList;

		private DictionaryTerm(String term, int pLSize, LinkedList<Index> indexList) {
			this.term = term;
			this.pLSize = pLSize;
			this.indexList = indexList;
		}

		private String getTerm() {
			return term;
		}

		@SuppressWarnings("unused")
		private void setTerm(String term) {
			this.term = term;
		}

		private Integer getpLSize() {
			return pLSize;
		}

		@SuppressWarnings("unused")
		private void setpLSize(Integer pLSize) {
			this.pLSize = pLSize;
		}

		private LinkedList<Index> getIndexList() {
			return indexList;
		}

		@SuppressWarnings("unused")
		private void setIndexList(LinkedList<Index> indexList) {
			this.indexList = indexList;
		}
	}
}
