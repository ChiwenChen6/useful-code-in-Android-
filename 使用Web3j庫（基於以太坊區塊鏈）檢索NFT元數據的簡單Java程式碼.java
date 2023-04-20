   // 引入必要的庫
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

// 創建一個異步任務以檢索NFT元數據
private class RetrieveMetadataTask extends AsyncTask<Void, Void, String> {
    private final String ethereumNodeUrl;
    private final String nftContractAddress;
    private final BigInteger tokenId;

    public RetrieveMetadataTask(String ethereumNodeUrl, String nftContractAddress, BigInteger tokenId) {
        this.ethereumNodeUrl = ethereumNodeUrl;
        this.nftContractAddress = nftContractAddress;
        this.tokenId = tokenId;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // 創建Web3j對象
            Web3j web3j = Web3j.build(new HttpService(ethereumNodeUrl));

            // 創建ERC721合約對象
            ERC721 contract = ERC721.load(nftContractAddress, web3j, new ReadonlyTransactionManager(web3j, null));

            // 使用合約對象檢索NFT的所有者地址
            Address ownerAddress = contract.ownerOf(new Uint256(tokenId)).send();

            // 使用合約對象檢索NFT的元數據URL
            String metadataUrl = contract.tokenURI(new Uint256(tokenId)).send();

            // 從metadataUrl中下載NFT的元數據
            String metadata = downloadUrl(metadataUrl);

            return metadata;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 下載指定URL的內容
    private String downloadUrl(String urlString) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // 開始連接
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // 將InputStream轉換為String
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // 下載完成後，在UI線程顯示元數據
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            //
            // 將元數據轉換為JSON對象
            JSONObject metadataJson = new JSONObject(result);

            // 從JSON對象中讀取NFT的名稱和圖像URL
            String name = metadataJson.getString("name");
            String imageUrl = metadataJson.getString("image");

            // 下載NFT的圖像
            Bitmap image = downloadImage(imageUrl);

            // 在ImageView中顯示NFT圖像
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(image);

            // 在TextView中顯示NFT名稱
            TextView textView = findViewById(R.id.textView);
            textView.setText(name);
        }
    }

    // 下載指定URL的圖像
    private Bitmap downloadImage(String urlString) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // 開始連接
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // 將InputStream轉換為Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
