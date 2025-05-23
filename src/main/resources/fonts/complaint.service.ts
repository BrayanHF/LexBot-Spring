import { inject, Injectable, signal } from '@angular/core';
import { blob } from 'node:stream/consumers';
import { DocumentIDType } from '../../conversation/enum/document-id-type.enum';
import { Message } from '../../conversation/interfaces/message.interface';
import { ComplaintRequest } from '../interfaces/complaint-request.interface';
import { DocumentGenerator } from '../interfaces/document-generator,interface';
import { DocumentId } from '../interfaces/document-id.interface';
import { GenerateDocumentService } from './generate-document.service';
import { getDocumentAbbreviation, titleCase } from './helper';
import { JsPdfService } from './js-pdf.service';

@Injectable({
  providedIn: 'root',
})
export class ComplaintService implements DocumentGenerator {

  private jsPdfService = inject(JsPdfService);
  private generateDocumentService = inject(GenerateDocumentService);

  public pdfName = signal<string | null>(null);
  public messages = signal<Message[]>([]);

  private document: DocumentId | null = null;
  private currentQuestionIndex = 0;
  private answers: string[] = [];

  private questionsComplaint: string[] = [
    "¿Cuál es el lugar de expedición de tu documento?", // expedition
    "¿Cuál es tu nombre completo?", // fullName
    "¿Cuál es tu número de teléfono de contacto?", // phone
    "¿Cuál es tu correo electrónico para que te notifiquen?", // email
    "¿En qué ciudad estás presentando esta petición?", // city
    "¿Cuál es la dirección de tu casa o dónde quieres que te respondan?", // address

    "¿A qué autoridad vas a enviar tu querella?", // authority
    "¿Cuál es el motivo o referencia de tu querella?", // reference

    "¿Cómo se llama la persona contra la que vas a presentar la querella?", // defendantName
    "¿Cuál es el documento de identidad o cómo describirías a esa persona si no conoces su cédula?", // defendantIdOrDesc
    "¿Conoces la dirección o datos de contacto de esa persona? Indícalos si los tienes.", // defendantContact

    "¿En qué consiste el conflicto o la conducta que reclamas? (explica con tus palabras)", // conflictDescription
    "¿Qué pruebas tienes para demostrar lo sucedido?", // mainEvidence

    "¿Intentaste hablar o notificar a la otra persona antes de esto? ¿Qué pasó?", // priorNotification
    "¿Qué quieres que haga la autoridad? (por ejemplo, ordenar cese de la conducta, reparar daños…)", // desiredOutcome

    "¿Qué documentos tienes para probar tu caso? (contratos, escrituras, fotos…)", // documents
    "¿Tienes otras pruebas? (videos, mensajes, testigos…)", // otherEvidence
    "¿Quieres que la autoridad ordene alguna diligencia especial? (inspección ocular, llamado a testigos…)", // specialDiligence

    "¿Dónde recibirás las notificaciones del proceso?", // notificationAddress
    "¿Tienes datos de notificación de la otra persona?", // defendantNotification

    "¿Vas a anexar documentos además de este generado? Si sí, ¿cuáles?" // additionalAttachments
  ];

  public start(document: DocumentId): void {
    this.generarLoQueSea();
    return;

    this.document = document;
    this.currentQuestionIndex = 0;
    this.answers = [];
    this.messages.set([]);
    this.pushAssistantMessage(this.questionsComplaint[0]);
  }

  public generarLoQueSea() {
    const text = "Señor Fiscal,\n" +
      "\n" +
      "**Referencia**  \n" +
      "Accidente de tránsito en el cual el responsable se dio a la fuga.\n" +
      "\n" +
      "**DATOS DEL QUERELLANTE**  \n" +
      "Brayan Hernandez, identificado con Cédula de ciudadanía No. 12121212, expedida en Cereté, residente en la dirección mz t lt 20, barrio El Poblado, Montería. Mi número de contacto es 3001719317 y mi correo electrónico para notificaciones es brayan@gmail.com.\n" +
      "\n" +
      "**DATOS DEL QUERELLADO**  \n" +
      "Se desconoce el nombre y documento de la persona responsable del accidente, ya que esta se dio a la fuga, y no se cuenta con información sobre su domicilio o datos de contacto.\n" +
      "\n" +
      "**HECHOS**  \n" +
      "PRIMERO: El día 19 de mayo de 2025, aproximadamente a las 3:00 p.m., ocurrió un accidente de tránsito en el cual mi motocicleta se vio involucrada. El responsable de dicho accidente se dio a la fuga, por lo que no fue posible obtener sus datos ni establecer comunicación con él.  \n" +
      "SEGUNDO: En el lugar del incidente se encontraba una cámara instalada en mi motocicleta que grabó el accidente y servirá como prueba del suceso. Dado que el responsable escapó del lugar, no se intentó notificarlo o contactarlo.  \n" +
      "TERCERO: Debido a la acción evasiva del responsable, me he visto afectado en la reparación de mi motocicleta y en los daños ocasionados, motivo por el cual solicito la intervención de la Fiscalía.\n" +
      "\n" +
      "**FUNDAMENTOS JURÍDICOS**  \n" +
      "De acuerdo con el Código Nacional de Policía, es deber de las autoridades velar por la convivencia pacífica y el respeto a la propiedad ajena, por lo que es procedente que se tomen las acciones pertinentes frente a este suceso. Además, se pueden invocar las normas del Código Civil en relación a la responsabilidad por daños.\n" +
      "\n" +
      "**PRETENSIONES**  \n" +
      "1. Solicito a la Fiscalía que se ordene la reparación de mi motocicleta a cargo del responsable del accidente.  \n" +
      "2. Que se me indemnice por los daños ocasionados.  \n" +
      "3. Que se imponga una multa al responsable una vez identificado.\n" +
      "\n" +
      "**PRUEBAS**  \n" +
      "1. Aportadas: Video del accidente grabado por la cámara de mi motocicleta.  \n" +
      "2. Otras: Registro visual del suceso que documenta claramente lo ocurrido.  \n" +
      "3. Solicitadas: No se requiere diligencia especial adicional.\n" +
      "\n" +
      "**NOTIFICACIONES**  \n" +
      "Para el querellante: Las notificaciones del proceso se recibirán en mz t lt 20, barrio El Poblado, Montería.  \n" +
      "Para el querellado: No se cuenta con datos de notificación de la persona responsable.\n" +
      "\n" +
      "**ANEXOS**  \n" +
      "No se anexarán documentos adicionales a este formulario. \n" +
      "\n" +
      "Atentamente,  \n" +
      "Brayan Hernandez."

    const complaintRequest: ComplaintRequest = {
      documentId: {
        number: '',
        error: '',
        expedition: '',
        type: DocumentIDType.CedulaCiudadania
      },
      fullName: "this.answers[1]",
      phone: 'this.answers[2]',
      email: 'this.answers[3]',
      city: 'this.answers[4]',
      address: 'this.answers[5]',

      authority: 'this.answers[6]',
      reference: 'this.answers[7]',

      defendantName: 'this.answers[8]',
      defendantIdOrDesc: 'this.answers[9]',
      defendantContact: 'this.answers[10]',

      conflictDescription: 'this.answers[11]',
      mainEvidence: 'this.answers[12]',
      priorNotification: 'this.answers[13]',
      desiredOutcome: 'this.answers[14]',

      documents: 'this.answers[15]',
      otherEvidence: 'this.answers[16]',
      specialDiligence: 'this.answers[17]',

      notificationAddress: 'this.answers[18]',
      defendantNotification: 'this.answers[19]',

      additionalAttachments: 'this.answers[20]',
    };

    this.generateDocumentService.testPdf(text).subscribe(
      pdfBlob => {
        const blob = new Blob([ pdfBlob ], { type: 'application/pdf' });
        const url = URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = 'QUERELLA_TEST.pdf';
        a.click();

        URL.revokeObjectURL(url);
      }
    )
  }


  public handleUserMessage(userAnswer: string): void {
    if (!this.document || this.currentQuestionIndex >= this.questionsComplaint.length) return;

    const currentQuestion = this.questionsComplaint[this.currentQuestionIndex];
    this.pushUserMessage(userAnswer);

    this.generateDocumentService.validateAnswerComplaint({ question: currentQuestion, answer: userAnswer }).subscribe({
      next: (validation) => {
        if (validation.error) {
          this.pushAssistantMessage(validation.error);
          return;
        }

        this.answers.push(validation.result ?? userAnswer);
        this.currentQuestionIndex++;

        if (this.currentQuestionIndex < this.questionsComplaint.length) {
          this.pushAssistantMessage(this.questionsComplaint[this.currentQuestionIndex]);
        } else {
          this.generateComplaint();
        }
      },
      error: () => {
        this.pushAssistantMessage('Hubo un error al validar tu respuesta. Intenta de nuevo más tarde.');
      }
    });
  }

  private generateComplaint(): void {
    if (!this.document) return;

    this.document.expedition = this.answers[0];

    const complaintRequest: ComplaintRequest = {
      documentId: this.document,
      fullName: this.answers[1],
      phone: this.answers[2],
      email: this.answers[3],
      city: this.answers[4],
      address: this.answers[5],

      authority: this.answers[6],
      reference: this.answers[7],

      defendantName: this.answers[8],
      defendantIdOrDesc: this.answers[9],
      defendantContact: this.answers[10],

      conflictDescription: this.answers[11],
      mainEvidence: this.answers[12],
      priorNotification: this.answers[13],
      desiredOutcome: this.answers[14],

      documents: this.answers[15],
      otherEvidence: this.answers[16],
      specialDiligence: this.answers[17],

      notificationAddress: this.answers[18],
      defendantNotification: this.answers[19],

      additionalAttachments: this.answers[20],
    };

    this.pushAssistantMessage('Generando documento, por favor espera...');
    this.generateComplaintPDF(complaintRequest);
  }

  public getPdf(): void {
    const filename = this.pdfName();
    if (filename) {
      this.jsPdfService.save(filename);
    }
  }

  public cleanAll(): void {
    this.jsPdfService.cleanPdf();
    this.pdfName.set(null);
    this.messages.set([]);
    this.document = null;
    this.currentQuestionIndex = 0;
    this.answers = [];
  }

  private generateComplaintPDF(request: ComplaintRequest): void {
    this.generateDocumentService.generateComplaintText(request).subscribe({
      next: (res) => {
        const bodyText = res.choices[0].response.content;
        if (bodyText) {
          this.generatePdfDocument(request, bodyText);
        } else {
          this.pushAssistantMessage('Error generando el texto del documento.');
        }
      },
      error: () => {
        this.pushAssistantMessage('Error al comunicarse con el servidor.');
      }
    });
  }

  private generatePdfDocument(request: ComplaintRequest, bodyText: string): void {
    this.jsPdfService.writeBlock(bodyText);
    this.jsPdfService.writeBlock('Cordialmente,');
    this.jsPdfService.writeBlock('');
    this.jsPdfService.writeBlock('_______________________________');
    this.jsPdfService.writeBlock(titleCase(request.fullName));
    this.jsPdfService.writeBlock(`${ getDocumentAbbreviation(request.documentId.type) }. ${ request.documentId.number }`);


    console.log(bodyText);
    this.pdfName.set(`Q_${ request.documentId.number }.pdf`);
  }

  private pushUserMessage(text: string): void {
    this.messages.update((s) => [ ...s, { id: '', role: 'user', text } ]);
  }

  private pushAssistantMessage(text: string): void {
    this.messages.update((s) => [ ...s, { id: '', role: 'assistant', text } ]);
  }

}
