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

import java.util.Objects;

/**
 * Represent a comment.
 */
public final class Comment extends Node {

    ResizableCharBuilder dataBuilder;
    private String data;

    public Comment(String data) {
        this.data = data;
    }

    Comment(ResizableCharBuilder dataBuilder) {
        this.dataBuilder = dataBuilder;
    }

    @Override
    public int getNodeType() {
        return COMMENT_NODE;
    }

    public String getData() {
        if (data == null && dataBuilder != null) {
            data = dataBuilder.asString();
            dataBuilder = null;
        }
        return data;
    }

    public void setData(String data) {
        this.data = data;
        this.dataBuilder = null;
    }

    /**
     * Return the {@link String} "#comment".
     */
    @Override
    public String getNodeName() {
        return "#comment";
    }

	@Override
	public Node cloneNode(boolean deep) {
		return new Comment(getData());
	}

    @Override
    public boolean isEqualNode(Node other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Comment) {
            return Objects.equals(getData(), ((Comment) other).getData());
        }
        return false;
    }
}
