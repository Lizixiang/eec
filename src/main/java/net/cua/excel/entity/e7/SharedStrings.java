/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.cua.excel.entity.e7;

import net.cua.excel.manager.Const;
import net.cua.excel.annotation.TopNS;
import net.cua.excel.util.FileUtil;
import net.cua.excel.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * Created by guanquan.wang at 2017/10/10.
 */
@TopNS(prefix = "", value = "sst", uri = Const.SCHEMA_MAIN)
public class SharedStrings {
    // 存储共享字符
    private Map<String, Integer> elements;
    private int count; // workbook所有字符串(shared属性为true)的个数
    private static final int MAX_CACHE_SIZE = 8192;

    SharedStrings() {
        elements = new LinkedHashMap<>();
    }

    ThreadLocal<char[]> charCache = ThreadLocal.withInitial(() -> new char[1]);
    public int get(char c) {
        char[] cs = charCache.get();
        cs[0] = c;
        return get(new String(cs));
    }

    /**
     * TODO 每个sheet采用one by one的方式输出，暂不考虑并发
     * @param key
     * @return
     */
    public int get(String key) {
        Integer n = elements.get(key);
        if (n == null) {
            if (elements.size() < MAX_CACHE_SIZE) {
                elements.put(key, n = elements.size());
            } else {
                return -1;
            }
        }
        count++;
        return n.intValue();
    }

    public void write(Path root) throws IOException {
        TopNS topNS = getClass().getAnnotation(TopNS.class);

        DocumentFactory factory = DocumentFactory.getInstance();
        //use the factory to create a root element
        Element rootElement = factory.createElement(topNS.value(), topNS.uri()[0]);
        rootElement.addAttribute("uniqueCount", String.valueOf(elements.size()));
        rootElement.addAttribute("count", String.valueOf(count));

        elements.forEach((k,v) -> rootElement.addElement("si").addElement("t").setText(k));

        Document doc = factory.createDocument(rootElement);
        FileUtil.writeToDiskNoFormat(doc, root.resolve(StringUtil.lowFirstKey(getClass().getSimpleName() + Const.Suffix.XML))); // write to desk

        // destroy
        destroy();
    }

    /**
     * clear memory
     */
    protected void destroy() {
        elements.clear();
        elements = null;
    }
}
