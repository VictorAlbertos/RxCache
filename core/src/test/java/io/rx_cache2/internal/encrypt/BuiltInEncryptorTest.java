/*
 * Copyright 2015 Victor Albertos
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

package io.rx_cache2.internal.encrypt;


import io.rx_cache2.internal.encrypt.Encryptor;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BuiltInEncryptorTest {
    private static final String FILENAME_IN = "fileIn";
    private static final String FILENAME_ENCRYPTED = "fileEncrypted";
    private static final String FILENAME_OUT = "fileOut";
    private Encryptor encryptor;
    private static File fileIn, fileOut, fileEncrypted;
    @ClassRule public static TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Before public void setUp() {
        encryptor = new io.rx_cache2.internal.encrypt.BuiltInEncryptor();
    }

    @Test public void _01_When_Encrypt_And_Decrypt_Then_Retrieve_Original_Content() {
        fileIn = getFile(FILENAME_IN, "A Dummy Content");
        fileOut = getFile(FILENAME_OUT);
        fileEncrypted = getFile(FILENAME_ENCRYPTED);

        encryptor.encrypt("key", fileIn, fileEncrypted);
        encryptor.decrypt("key", fileEncrypted, fileOut);
        assertEquals(getFileContent(fileIn), getFileContent(fileOut));
    }

    @Test public void _02_When_Encrypt_Then_Original_Content_Does_Not_Match() {
        fileIn = getFile(FILENAME_IN, "A Dummy Content 2");
        fileEncrypted = getFile(FILENAME_ENCRYPTED);

        encryptor.encrypt("key2", fileIn, fileEncrypted);
        assertNotEquals(getFileContent(fileIn), getFileContent(fileEncrypted));
    }

    @Test public void _03_When_Decrypt_Then_Original_Content_Does_Match() {
        fileOut = getFile(FILENAME_OUT);

        encryptor.decrypt("key2", fileEncrypted, fileOut);
        assertEquals(getFileContent(fileIn), (getFileContent(fileOut)));
    }

    @Test public void _04_When_Decrypt_With_Other_Key_Then_Original_Content_Does_Not_Match() {
        fileOut = getFile(FILENAME_OUT);

        encryptor.decrypt("keyFail", fileEncrypted, fileOut);
        assertNotEquals(getFileContent(fileIn), (getFileContent(fileOut)));
    }

    @Test public void _05_When_Decrypt_With_Original_Key_Then_Original_Content_Does_Match() {
        fileOut = getFile(FILENAME_OUT);

        encryptor.decrypt("key2", fileEncrypted, fileOut);
        assertEquals(getFileContent(fileIn), (getFileContent(fileOut)));
    }

    @Test public void _06_When_Encrypt_And_Decrypt_With_Long_Key_Then_Retrieve_Original_Content() {
        String key2048 = "[n(s4v7nf?~]k'bP$/m]tc.VYXN2;H@C<~qL[{?LX!s$#$(zMh&ec}2R9$n.Zp)2%c}chbfQ5;#[t@7J\".\\-a\\SnBZGMF/VLHmc/c5yZ-<;7n(Eb4xJE%{CXX\"&msz{fVdT2HJN7Hvf(Eh>{fc2M8,}\"z8/;Ycv(H~./Lq-Q`=`N]9_T$[WmQKnKYu$MLf$6&]V`>k.DaxEfFgeZ:`uwZ^p,42-%ekT\"ef`DG]mjhK>_.6dTM}p`kgeZ%B$$H^Pz$[5$^x()t'@k?F=DX',cHj~ag%yFCm?m>u>xN2-R.WWb{zcH6\\Qe63PA[w<?ssg,@hRXeN6h[*yqYA6`;]K2j=3Z_}Gg)Rpz$r@4+`Q3?uF'n/xP]K(va&tRB22^*]}\\m9H@]<SZ=\">G=y[fWBAJp^4%-J2ju_>^}K^?`9!(CU[W9s(Zgvkx*x)c'*/]7kKU@{&wh<@mQVk^M(e}F?_]sYBR;G/J_k>!LkzW2c-~#q75bbwZ}]K{;@yg`[%\\ksHwJb]ugxP]zUz73Ur7?'S-~4L`c+m/'Tu{b&#SYSDQ!nvWaeV6Z\\[~yD<^j7aV>/RK(&gS(g9x,EG+9%w}W5LE~vNYKQj3)SM%pQ;V;U3NB.Se\\qF9wc>_~=r)aQ'd7<,\\3Xmze^N=38c2{N\"dQQX;}-)C?b[GMWb2d(8:/4j*!<VW9wE~;[umE@Z2]Nz='7CrF3F(9W5rS;WmpL$R4ZAbBtG`3r!4U/HuA3V@t}-mkbnfmk7qmzcr\\F'fe-fWBVEx.F2Q37\\T:&$u,,~7HFx7HeHD<7~\\M@*S*L&2>N+'zBTes.6GW]mW&eQ&v,zE/9\"w2<Z`Ap?s]yCj@3W&~#nUgx3b%#@_V_ZHWxf8W6^g\\V#L+s@Ra8ug-U7D(gK;S*>_GT8($ZVjv?@BzVeyZqH\\7y?bX?ZZ[Aq3Km=Jr.(2SMgBVd$$T[Qq`mHdJVM>4FRcL{a,Yv\"3=&SVrP:_[Bpk?Wq4/ZSGG:/;PD$3;gEu~w?k/}/DQZm9T{p=4&F#.VG+;7~w\\.\\/]$wvUQ-38=Ejq+4He7:X&$ZN]F=GHM<^Ftw/pt/]L`>$&6^z(3p4MhHt&xu,Eem>}~bfn+^mBY<$RS}q:&n\\qZ[#&G^N6m\\j<U]k@63N$ThU65A&G_aN$a9-Tgp]CYH!vV'EG_5eqgH-WRRu!Q~@2nE93D\"N@<S7+32j-@{,mU{x#$#ZF'z6C+HQPPw/#]y'VPnqx=-cm9M^6Xh:\")wj*tS~ge@N>&a#->cGzkjDLH6#!55,7Z6+-UBF49=3N+u^CPG*>EsXk7'X{AELfD:pb`!D5v'=}G;7xSZ'&6~=Tp2v'V(UKM)b[Q+^+;{=#2+J*tA~!AH\"VbD;%8nd_S3,+Z]vzKQss5:\\tJ38B^&6*j@~[W_sLR5<!(aXz\"6%DJ8vr'3$:=\\Unf44PB5[?L%J'D)~P$$njSt.UDV5NM'm=EjNry:t{tg8Mn*6v@S@'?V!&_wxRUP3Fh`]uZg^\\E5H:9r#2(UWbnxfa7.,_@u;V,2(/9:JE_BZb`V59N/kq(K<tJz'~_H[k;c\"E#!j9G[M(=#vyt'K2hAMe?+WsA,cu,QVL9]EV&fW~&XNThRL][8#LpR`K?UDdqHY+\"G\"K(PLf@ew+^&rB9pn%q#9h[\\5hbj7WKVLr2V!&nG~<DKnL8H6McGUn3]q;%*:\"{^f\"C8w^&w8B$B,Q#J\\j{bN+Xqq.VJ>:m=LJ^_:2f,_:U7k!N^dH!D4p'Rj}#E)%rZwb,{%Tc@a!^f$Q=a]T((k^R^Gy.4us&;RhDaBZwd>`?p>-MU:/F;d>u`Z8YL_Rz`\\}`t8GzWfqYecrw3N@=]rXn6;*_<r;/_/yCjPTVFp{*nVmEDryDkf`_vU^~6?m-rGNnk9u)t(`hsw;2=^m,?+r}&Sw[&`qDUDvVv:<u2d2PRF^p?#kLz7\"\\*5Xf=RrTDM;Sj8>M}}5>4QBa=w)}M[%v9\\NEy%C6?m[WhSZVA3gztd^Ps;9%/__@xk\"5Je6?FbSn=V_:C{BRk%vqqE/ruFFqeDPrKYB\\r=Kag75M<y";
        fileIn = getFile(FILENAME_IN, "A Dummy Content");
        fileOut = getFile(FILENAME_OUT);
        fileEncrypted = getFile(FILENAME_ENCRYPTED);

        encryptor.encrypt(key2048, fileIn, fileEncrypted);
        encryptor.decrypt(key2048, fileEncrypted, fileOut);
        assertEquals(getFileContent(fileIn), getFileContent(fileOut));
    }

    @Test public void _07_When_Encrypt_And_Decrypt_With_Long_Key_And_Long_Content_Then_Retrieve_Original_Content() {
        String key2048 = "[n(s4v7nf?~]k'bP$/m]tc.VYXN2;H@C<~qL[{?LX!s$#$(zMh&ec}2R9$n.Zp)2%c}chbfQ5;#[t@7J\".\\-a\\SnBZGMF/VLHmc/c5yZ-<;7n(Eb4xJE%{CXX\"&msz{fVdT2HJN7Hvf(Eh>{fc2M8,}\"z8/;Ycv(H~./Lq-Q`=`N]9_T$[WmQKnKYu$MLf$6&]V`>k.DaxEfFgeZ:`uwZ^p,42-%ekT\"ef`DG]mjhK>_.6dTM}p`kgeZ%B$$H^Pz$[5$^x()t'@k?F=DX',cHj~ag%yFCm?m>u>xN2-R.WWb{zcH6\\Qe63PA[w<?ssg,@hRXeN6h[*yqYA6`;]K2j=3Z_}Gg)Rpz$r@4+`Q3?uF'n/xP]K(va&tRB22^*]}\\m9H@]<SZ=\">G=y[fWBAJp^4%-J2ju_>^}K^?`9!(CU[W9s(Zgvkx*x)c'*/]7kKU@{&wh<@mQVk^M(e}F?_]sYBR;G/J_k>!LkzW2c-~#q75bbwZ}]K{;@yg`[%\\ksHwJb]ugxP]zUz73Ur7?'S-~4L`c+m/'Tu{b&#SYSDQ!nvWaeV6Z\\[~yD<^j7aV>/RK(&gS(g9x,EG+9%w}W5LE~vNYKQj3)SM%pQ;V;U3NB.Se\\qF9wc>_~=r)aQ'd7<,\\3Xmze^N=38c2{N\"dQQX;}-)C?b[GMWb2d(8:/4j*!<VW9wE~;[umE@Z2]Nz='7CrF3F(9W5rS;WmpL$R4ZAbBtG`3r!4U/HuA3V@t}-mkbnfmk7qmzcr\\F'fe-fWBVEx.F2Q37\\T:&$u,,~7HFx7HeHD<7~\\M@*S*L&2>N+'zBTes.6GW]mW&eQ&v,zE/9\"w2<Z`Ap?s]yCj@3W&~#nUgx3b%#@_V_ZHWxf8W6^g\\V#L+s@Ra8ug-U7D(gK;S*>_GT8($ZVjv?@BzVeyZqH\\7y?bX?ZZ[Aq3Km=Jr.(2SMgBVd$$T[Qq`mHdJVM>4FRcL{a,Yv\"3=&SVrP:_[Bpk?Wq4/ZSGG:/;PD$3;gEu~w?k/}/DQZm9T{p=4&F#.VG+;7~w\\.\\/]$wvUQ-38=Ejq+4He7:X&$ZN]F=GHM<^Ftw/pt/]L`>$&6^z(3p4MhHt&xu,Eem>}~bfn+^mBY<$RS}q:&n\\qZ[#&G^N6m\\j<U]k@63N$ThU65A&G_aN$a9-Tgp]CYH!vV'EG_5eqgH-WRRu!Q~@2nE93D\"N@<S7+32j-@{,mU{x#$#ZF'z6C+HQPPw/#]y'VPnqx=-cm9M^6Xh:\")wj*tS~ge@N>&a#->cGzkjDLH6#!55,7Z6+-UBF49=3N+u^CPG*>EsXk7'X{AELfD:pb`!D5v'=}G;7xSZ'&6~=Tp2v'V(UKM)b[Q+^+;{=#2+J*tA~!AH\"VbD;%8nd_S3,+Z]vzKQss5:\\tJ38B^&6*j@~[W_sLR5<!(aXz\"6%DJ8vr'3$:=\\Unf44PB5[?L%J'D)~P$$njSt.UDV5NM'm=EjNry:t{tg8Mn*6v@S@'?V!&_wxRUP3Fh`]uZg^\\E5H:9r#2(UWbnxfa7.,_@u;V,2(/9:JE_BZb`V59N/kq(K<tJz'~_H[k;c\"E#!j9G[M(=#vyt'K2hAMe?+WsA,cu,QVL9]EV&fW~&XNThRL][8#LpR`K?UDdqHY+\"G\"K(PLf@ew+^&rB9pn%q#9h[\\5hbj7WKVLr2V!&nG~<DKnL8H6McGUn3]q;%*:\"{^f\"C8w^&w8B$B,Q#J\\j{bN+Xqq.VJ>:m=LJ^_:2f,_:U7k!N^dH!D4p'Rj}#E)%rZwb,{%Tc@a!^f$Q=a]T((k^R^Gy.4us&;RhDaBZwd>`?p>-MU:/F;d>u`Z8YL_Rz`\\}`t8GzWfqYecrw3N@=]rXn6;*_<r;/_/yCjPTVFp{*nVmEDryDkf`_vU^~6?m-rGNnk9u)t(`hsw;2=^m,?+r}&Sw[&`qDUDvVv:<u2d2PRF^p?#kLz7\"\\*5Xf=RrTDM;Sj8>M}}5>4QBa=w)}M[%v9\\NEy%C6?m[WhSZVA3gztd^Ps;9%/__@xk\"5Je6?FbSn=V_:C{BRk%vqqE/ruFFqeDPrKYB\\r=Kag75M<y";
        fileIn = getFile(FILENAME_IN, key2048, 10000);  // 20 Mb aprox
        fileOut = getFile(FILENAME_OUT);
        fileEncrypted = getFile(FILENAME_ENCRYPTED);

        encryptor.encrypt(key2048, fileIn, fileEncrypted);
        encryptor.decrypt(key2048, fileEncrypted, fileOut);
        assertEquals(getFileContent(fileIn), getFileContent(fileOut));
    }

    private File getFile(String filename) {
        return getFile(filename, "");
    }

    private File getFile(String filename, String content) {
        return getFile(filename, content, 1);
    }

    private File getFile(String filename, String content, int iterations) {
        File file = new File(temporaryFolder.getRoot().getAbsolutePath(), filename);
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file, false);

            for (int i = 0; i < iterations; i++) {
                if (i == 0)
                    fileWriter.write(content);
                else
                    fileWriter.append(content);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    private String getFileContent(File file) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String aux;

            while ((aux = reader.readLine()) != null) {
                builder.append(aux);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }
}
