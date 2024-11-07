package guild.world;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeTest {

    @Test
    void build() {
        Tree<String> temp = Tree.<String>build(
                TreeNode.<String>branchNode("testID",
                        TreeNode.<String>leafNode("leafNode1", "testData"),
                        TreeNode.<String>leafNode("leafNode2", "testData2"),
                        TreeNode.<String>leafNode("leafNode3", "testData3")
                ),
                TreeNode.<String>branchNode("testID1",
                        TreeNode.<String>leafNode("testData1"),
                        TreeNode.<String>leafNode("testData4")
                )
        );
        temp.merge(new String[]{"root", "testID", "MergedNode"}, "MergedValue");
        temp.merge("testID:MergedNode", "MergedValue2");
        System.out.println(temp);
        temp.toList().forEach(each -> System.out.println(each.getNamespace() + " " + each.getData()));

        System.out.println(temp.lookup("testID:MergedNode").getLeaves());

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            final String tmpJson = mapper.writeValueAsString(temp);
            System.out.println(tmpJson);
            assertEquals(temp, mapper.readValue(tmpJson, Tree.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}