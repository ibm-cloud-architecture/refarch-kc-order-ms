package it;

import org.junit.Test;

/**
 * This is to test command retry:
 * 
 * When command agent does not save to the database successfully it does not commit its offset
 * to kafka so it can reload from the last committed offset.
 * 
 * @author jerome boyer
 *
 */
public class OrderServiceAdminIT extends CommonITTest {

    private String port = System.getProperty("liberty.test.port");
    private String endpoint = "/orders";
    private String url = "http://localhost:" + port + endpoint;

    @Test
    public void shouldNotCommitOffset() throws Exception {
       
    }

    

}
