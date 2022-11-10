#####  实现三个方法，分别通过以下方案将字符串写入文件中，将文件命名为该字符串的SHA-1值。

- 方案1：用FileWriter或FileOutputStream直接写字符串或其对应的字节流。

- 方案2：用DataOutputStream的writeInt, writeUTF等方法写入文件。

- 方案3：实现Blob类（包含type.size. content等属性）并为其实现Serializable接口，使用该字符串实例化Blob对象并用ObjectOutputStream将该对象序列化到文件中。
