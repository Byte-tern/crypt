package cryptography
//import java.util.Scanner
import java.io.File
import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO
import kotlin.random.Random

const val ENDING = "000000000000000000000011"

fun decoder(passwd : String, base : String) : String {
    var index = 0
    var resBase = ""
    var dBase : Int
    var dPwd : Int
    var item : Int
    val pwdLen = passwd.length
    for (i in 0..<base.length) {
        if(base[i] == '1') dBase=1
        else dBase=0
        if(passwd[index] == '1') dPwd=1
        else dPwd=0
        if ((dBase == 1) xor (dPwd == 1)) item=1
        else item=0
        resBase += item.toString()
        index++
        index %= pwdLen
    }
    return resBase
}

fun convToIntL8(str : String) : Int {
    val st = str.reversed()
    var res = 0
    var dou = 1
    for (i in 0..<8){
        var delta : Int
        if(st[i] == '1') delta=1
        else delta=0
        res+=dou*delta
        dou*=2
    }
    return res
}

fun convToByteL8(num : Int) : String {
    var res = ""
    var i : Int
    var cou = 0
    var usage = num
    while (usage>0) {
        i = usage%2
        res+=i.toString()
        cou++
        if (i == 1){
            usage--
        }
        usage/=2
    }
    while(cou<8){
        res+='0'
        cou++
    }
    return res.reversed()
}

fun convToByteL16(num : Int) : String {
    var res = ""
    var i : Int
    var usage = num
    while (usage>0) {
        i = usage%2
        res+=i.toString()
        if (i == 1){
            usage--
        }
        usage/=2
    }
    return res.reversed()
}

fun conv(message : String) : String {
    var res = ""
    val byteArr = message.toByteArray()
    for (it in byteArr) {
        res+=convToByteL8(it.toInt())
        //println(it.toInt())
    }
    return res
}

fun hide(){
    // Printing everything
    print("The path of input image file:\n> ")
    val loc = readln() // setting origin location
    print("The path of output image file:\n> ")
    val des = readln() // setting destination
    print("The message:\n> ")
    val mes = readln()

    // Making a password
    val random = Random.Default
    val pwd: Int = random.nextInt(from = 32768, until = 65535)

    // Making links to files
    val locFile = File(loc)
    val desFile = File(des)

    // Dealing with non-existing input document
    if (!locFile.exists()){
        println("Can't read input file!")
        return
    }

    // Confirmation messages
    println("Input file path: $loc")
    println("Output file path: $des")
    println("Your message: $mes")
    println("Encrypting password: $pwd")

    // User approval
    print("Type \"start\" to submit the operation, any other input will return you to the main menu:\n> ")
    val x = readln()
    if (x != "start") {
        println("Exiting to the menu...")
        return
    }

    // Converting message and password to a binary string
    val coder = convToByteL16(pwd)
    val byteMes = decoder(coder, conv(mes)) + ENDING
    println("Starting program...")

    try {
        // Reading input file
        val locImage: BufferedImage = ImageIO.read(locFile)

        // Checking our ability to write a message
        if (locImage.width*locImage.width<byteMes.length){
            println("The image is too small. Returning to the menu...")
            return
        }

        // Adding indexer
        var i = 0

        // Writing a message to a file
        for (x in 0 until locImage.width){
            for (y in 0 until locImage.height){
                val color = Color(locImage.getRGB(x, y))
            // test location - D:\download\reaver_upper_front.png
                val r = color.red
                val g = color.green
                val b = color.blue
                var blueByte = convToByteL8(b)
                if (i != -1) blueByte=blueByte.substring(0, 7) + byteMes[i]
                var blueNew = b
                if (i == -1) blueNew = b
                else blueNew = convToIntL8(blueByte)
                val colorNew = Color(r, g, blueNew)
                locImage.setRGB(x, y, colorNew.rgb)
                i++
                if(i>=byteMes.length || i==0) i=-1
            }
        }
        ImageIO.write(locImage, "png", desFile)
        println("File saved.")
    } catch (e: Exception) {
        println("Something went wrong. Returning to the menu...")
        return
    }
}

fun show() {
    // Printing everything
    print("The path of decoded image file:\n> ")
    val loc = readln() // setting origin location
    print("Input your given password:\n> ")
    val pwd = readln().toInt() // setting destination

    // Making links to files
    val locFile = File(loc)

    // Dealing with non-existing input document
    if (!locFile.exists()){
        println("Can't read input file!")
        return
    }

    try {
        // Reading input file
        val locImage: BufferedImage = ImageIO.read(locFile)

        // Adding indexer and result
        var res = ""

        // Reading a message from a file
        for (x in 0 until locImage.width){
            for (y in 0 until locImage.height){
                val color = Color(locImage.getRGB(x, y))
                // test location - D:\download\reaver_upper_front.png 50448
                val b = color.blue
                val blueByte = convToByteL8(b)
                if (blueByte[7] == '1') res+='1'
                else res +='0'
            }
        }

        val resultat = res.substringBefore(ENDING)
        //println(resultat)
        if (res == resultat || resultat.length%8 != 0){
            println("Nothing is hidden here. Returning to the main menu...")
            return
        }
        var byteArr = byteArrayOf()
        val resul = decoder(convToByteL16(pwd), resultat)
        for (index in 0..<(resul.length)/8){
            val subStr = resul.substring(index*8, (index+1)*8)
            val subByte = convToIntL8(subStr)
            byteArr+=subByte.toByte()
        }
        val mes = String(byteArr)
        println("Found message: \n\n$mes\n")

    } catch (e: Exception) {//55328
        println(e.message)//"Something went wrong. Returning to the menu...")
        return
    }
}

fun main() {
    var f=1
    var com : String
    //println(conv("Hello, World!"))
    while(f==1) {//-116, 24, 48, 97, -61, -122, 12, 24, 48, 97, -62, -123, 10, 20, 41, 82, -92, 72, -111, 35, 71, -113, 31, 62, 125, -5, -9, -17, -34, -68, 121, -14, -27, -54, -107, 43, 86, -84, 89, -78
        print("Task (hide, show, exit):\n> ")
        //val scanner = Scanner(System.`in`)
        com = readln()
        //scanner.close()
        when(com){
            "hide" -> {
                println("Hiding message in image.")
                hide()
            }
            "show" -> {
                println("Obtaining message from image.")
                show()
            }
            "exit" -> {
                println("Bye!")
                f=0
            }
            else -> println("Wrong task: $com")
        }
    }
}
