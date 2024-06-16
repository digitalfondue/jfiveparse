/**
 * Copyright © 2015 digitalfondue (info@digitalfondue.ch)
 *
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
package ch.digitalfondue.jfiveparse;

/**
 * Represent a text node.
 */
public class Text extends Node {

    ResizableCharBuilder dataBuilder;
    private String data;

    Text(ResizableCharBuilder dataBuilder) {
        this.dataBuilder = dataBuilder;
    }

    public Text() {
        this.data = "";
    }

    public Text(String data) {
        this.data = data;
    }

    @Override
    public byte getNodeType() {
        return TEXT_NODE;
    }

    public String getData() {
        if (dataBuilder != null) {
            data = dataBuilder.asString();
            dataBuilder = null;
        }
        return data;
    }

    public void setData(String data) {
        this.dataBuilder = null;
        this.data = data;
    }

    /**
     * Return the {@link String} "#text".
     */
    @Override
    public String getNodeName() {
        return "#text";
    }

	@Override
	public Node cloneNode(boolean deep) {
		return new Text(getData());
	}
}
