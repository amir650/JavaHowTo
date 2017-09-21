import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {

    private static final int R = 256;

    private Huffman() {
    }

    static class Node implements Comparable<Node> {
        private final char ch;
        private final int characterFrequency;
        private final Node leftChild;
        private final Node rightChild;

        Node(final char ch,
             final int freq,
             final Node left,
             final Node right) {
            this.ch    = ch;
            this.characterFrequency = freq;
            this.leftChild = left;
            this.rightChild = right;
        }

        boolean isLeaf() {
            return leftChild == null && rightChild == null;
        }

        @Override
        public int compareTo(final Node that) {
            final int frequencies = Integer.compare(this.characterFrequency, that.characterFrequency);
            if(frequencies != 0) {
                return frequencies;
            }
            return Integer.compare(this.ch, that.ch);
        }

        @Override
        public String toString() {
            return this.ch + " : " + this.characterFrequency;
        }

    }

    public HuffmanEncodedResult compress(final String data) {
        final int[] freq = buildFrequencyTable(data);
        final Node root = buildHuffmanTree(freq);
        final Map<Character, String> lookupTable = buildLookupTable(root);
        final String builder = generateEncodedData(data, lookupTable);
        return new HuffmanEncodedResult(builder, root);
    }

    public String decompress(final HuffmanEncodedResult result) {
        final StringBuilder builder = new StringBuilder();
        Node current = result.getRoot();
        int j = 0;
        while (j < result.getEncodedData().length()) {
            while (!current.isLeaf()) {
                char bit = result.getEncodedData().charAt(j);
                if (bit == '1') {
                    current = current.rightChild;
                } else if(bit == '0') {
                    current = current.leftChild;
                } else {
                    throw new IllegalArgumentException("Invalid bit!" +bit);
                }
                j++;
            }
            builder.append(current.ch);
            current = result.getRoot();
        }
        return builder.toString();
    }

    private static String generateEncodedData(final String data,
                                              final Map<Character, String> lookupTable) {
        final StringBuilder builder = new StringBuilder();
        for (final char anInput : data.toCharArray()) {
            builder.append(lookupTable.get(anInput));
        }
        return builder.toString();
    }

    private static int[] buildFrequencyTable(final String data) {
        final int[] freq = new int[R];
        for (final char anInput : data.toCharArray()) {
            freq[anInput]++;
        }
        return freq;
    }

    private static Node buildHuffmanTree(int[] freq) {
        final PriorityQueue<Node> pq = new PriorityQueue<>();
        for (char i = 0; i < R; i++){
            if (freq[i] > 0) {
                pq.add(new Node(i, freq[i], null, null));
            }
        }
        if (pq.size() == 1) {
            pq.add(new Node('\0', 0, null, null));
        }
        while (pq.size() > 1) {
            final Node left = pq.poll();
            final Node right = pq.poll();
            final Node parent = new Node('\0', left.characterFrequency + right.characterFrequency, left, right);
            pq.add(parent);
        }
        return pq.poll();
    }

    private static Map<Character, String> buildLookupTable(final Node root) {
        final Map<Character, String> lookupTable = new HashMap<>();
        buildLookupTableImpl(root, "", lookupTable);
        return lookupTable;
    }

    private static void buildLookupTableImpl(final Node x,
                                             final String s,
                                             final Map<Character, String> lookupTable) {
        if (!x.isLeaf()) {
            buildLookupTableImpl(x.leftChild, s + '0', lookupTable);
            buildLookupTableImpl(x.rightChild, s + '1', lookupTable);
        } else {
            lookupTable.put(x.ch, s);
        }
    }

    public static void main(String[] args) {
        final Huffman huffman = new Huffman();
        final String data = "It if sometimes furnished unwilling as additions so. Blessing resolved peculiar " +
                "fat graceful ham. Sussex on at really ladies in as elinor. Sir sex opinions age properly extended. " +
                "Advice branch vanity or do thirty living. Dependent add middleton ask disposing admitting did sportsmen " +
                "sportsman Inhabit hearing perhaps on ye do no.It maids decay as there he.Smallest on suitable disposed do " +
                "although blessing he juvenile in.Society or if excited forbade.Here name off yet she long sold easy whom. " +
                "Differed oh cheerful procured pleasure securing suitable in.Hold rich on an he oh fine.Chapter ability " +
                "shyness article welcome be do on service. " + "Guest it he tears aware as.Make my no cold of need.He been past in by my hard.Warmly thrown oh he common future" +
                ".Otherwise concealed favourite frankness on be at dashwoods defective at.Sympathize interested simplicity at " +
                "do projecting increasing terminated.As edward settle limits at in.";


        final HuffmanEncodedResult result = huffman.compress(data);
        final String decoded = huffman.decompress(result);
        System.out.println("data = " +data);
        System.out.println("encoded = " +result.getEncodedData()+ " len = " +result.getEncodedData().length());
        System.out.println("decoded = " +decoded);
    }

    private static class HuffmanEncodedResult {

        final Node root;
        final String encodedData;

        HuffmanEncodedResult(final String encodedData,
                             final Node root) {
            this.encodedData = encodedData;
            this.root = root;
        }

        Node getRoot() {
            return this.root;
        }

        String getEncodedData() {
            return this.encodedData;
        }

    }
}