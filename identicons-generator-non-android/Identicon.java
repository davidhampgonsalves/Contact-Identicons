import com.davidhampgonsalves.identicon.*;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Identicon {

	public static void main(String [] args) throws IOException {
		
		if(args.length != 1) {
			System.err.println("usage: user name to generate identicon from.");
			return;
		}

		//get input string
		String userName = args[0];

		HashGeneratorInterface hashGenerator = new MessageDigestHashGenerator("MD5");

		BufferedImage identicon = IdenticonGenerator.generate(userName, hashGenerator);
		
		//save identicon to file
		ImageIO.write(identicon,"PNG",new File(userName + ".png"));
	}
}