package flandre.cn.novel.bean.data.activity;


import com.chad.library.adapter.base.entity.MultiItemEntity;

public class ConfigureThemeData implements MultiItemEntity {
    public static final int HEAD_TYPE = 0;
    public static final int EDIT_TYPE = 1;
    public static final int ONE_CHOICE_TYPE = 2;
    public static final int MULTI_CHOICE_TYPE = 3;

    private int type;

    private Head head = null;
    private Edit edit = null;
    private OneChoice oneChoice = null;
    private MultiChoice multiChoice = null;

    public ConfigureThemeData(Head head) {
        this.head = head;
        type = HEAD_TYPE;
    }

    public ConfigureThemeData(Edit edit) {
        this.edit = edit;
        type = EDIT_TYPE;
    }

    public ConfigureThemeData(OneChoice oneChoice) {
        this.oneChoice = oneChoice;
        type = ONE_CHOICE_TYPE;
    }

    public ConfigureThemeData(MultiChoice multiChoice) {
        this.multiChoice = multiChoice;
        type = MULTI_CHOICE_TYPE;
    }

    public Head getHead() {
        return head;
    }

    public Edit getEdit() {
        return edit;
    }

    public OneChoice getOneChoice() {
        return oneChoice;
    }

    public MultiChoice getMultiChoice() {
        return multiChoice;
    }

    @Override
    public int getItemType() {
        return type;
    }

    public static class Head{
        private String headText;

        public Head(String headText) {
            this.headText = headText;
        }

        public String getHeadText() {
            return headText;
        }
    }

    public static class Edit{
        private String introduce;
        private String editString;

        public Edit(String introduce, String editString) {
            this.introduce = introduce;
            this.editString = editString;
        }

        public String getIntroduce() {
            return introduce;
        }

        public String getEditString() {
            return editString;
        }

        public void setEditString(String editString) {
            this.editString = editString;
        }
    }

    public static class OneChoice{
        private boolean isSelected;
        private String name;
        private String source;

        public OneChoice(String name, String source, boolean isSelected) {
            this.isSelected = isSelected;
            this.name = name;
            this.source = source;
        }

        public String getSource() {
            return source;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }

    public static class MultiChoice{
        private boolean isSelected;
        private String name;

        public MultiChoice(String name, boolean isSelected) {
            this.isSelected = isSelected;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }
}
