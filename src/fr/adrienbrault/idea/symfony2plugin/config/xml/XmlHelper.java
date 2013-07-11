package fr.adrienbrault.idea.symfony2plugin.config.xml;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import fr.adrienbrault.idea.symfony2plugin.dic.ServiceMap;
import fr.adrienbrault.idea.symfony2plugin.dic.ServiceMapParser;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

public class XmlHelper {
    public static PsiElementPattern.Capture<PsiElement> getTagPattern(String... tags) {
        return XmlPatterns
            .psiElement()
            .inside(XmlPatterns
                .xmlAttributeValue()
                .inside(XmlPatterns
                    .xmlAttribute()
                    .withName(StandardPatterns.string().oneOfIgnoreCase(tags)
                    )
                )
            );
    }

    /**
     * <tag attributeNames="|"/>
     *
     * @param tag tagname
     * @param attributeNames attribute values listen for
     */
    public static PsiElementPattern.Capture<PsiElement> getTagAttributePattern(String tag, String... attributeNames) {
        return XmlPatterns
            .psiElement()
            .inside(XmlPatterns
                .xmlAttributeValue()
                .inside(XmlPatterns
                    .xmlAttribute()
                    .withName(StandardPatterns.string().oneOfIgnoreCase(attributeNames))
                    .withParent(XmlPatterns
                        .xmlTag()
                        .withName(tag)
                    )
                )
            );
    }

    public static PsiElementPattern.Capture<PsiElement> getParameterWithClassEndingPattern() {
        return XmlPatterns
            .psiElement()
            .withParent(XmlPatterns
                .xmlText()
                .withParent(XmlPatterns
                    .xmlTag()
                    .withName("parameter").withChild(
                        XmlPatterns.xmlAttribute("key").withValue(
                            XmlPatterns.string().endsWith(".class")
                        )
                    )
                )
            );
    }

    @Nullable
    public static ServiceMap getLocalMissingServiceMap(PsiElement psiElement,@Nullable Map<String, String> currentServiceMap) {
        try {
            VirtualFile virtualFile = psiElement.getContainingFile().getOriginalFile().getVirtualFile();
            if(virtualFile != null) {
                ServiceMap localServiceMap = new ServiceMapParser().parse(virtualFile.getInputStream());
                ServiceMap unknownServiceMap = new ServiceMap();
                for(Map.Entry<String, String> entry: localServiceMap.getPublicMap().entrySet()) {
                    if(currentServiceMap == null) {
                        unknownServiceMap.getMap().put(entry.getKey(), entry.getValue());
                    } else if ( !currentServiceMap.containsKey(entry.getKey())) {
                        unknownServiceMap.getMap().put(entry.getKey(), entry.getValue());
                    }
                }

                return unknownServiceMap;
            }

        } catch (SAXException ignored) {
        } catch (IOException ignored) {
        } catch (ParserConfigurationException ignored) {
        }

        return null;
    }
}
