const pptxgen = require("pptxgenjs");

const GREEN = "2F6B4F";
const DARK = "1E2A22";
const LIGHT_BG = "F4F6F5";
const MUTED = "55665A";
const WHITE = "FFFFFF";
const CARD = "EDF2EE";

let pres = new pptxgen();
pres.layout = "LAYOUT_WIDE"; // 13.3 x 7.5

function baseSlide() {
  const s = pres.addSlide();
  s.background = { color: WHITE };
  return s;
}

function title(s, text, opts = {}) {
  s.addText(text, {
    x: 0.6, y: 0.5, w: 12.1, h: 0.9,
    fontSize: 30, bold: true, color: DARK, fontFace: "Arial",
    ...opts,
  });
}

function placeholder(s, x, y, w, h, label) {
  s.addShape("rect", { x, y, w, h, fill: { color: CARD }, line: { color: "C7D0CA", width: 1, dashType: "dash" } });
  s.addText(label, {
    x, y, w, h, align: "center", valign: "middle",
    fontSize: 13, color: MUTED, italic: true, fontFace: "Arial",
    margin: 0,
  });
}

// ---------- Slide 1: Title ----------
{
  const s = baseSlide();
  s.background = { color: GREEN };
  s.addText("Panchayat Grievance Register\n& Resolution Tracker", {
    x: 0.8, y: 2.3, w: 11.7, h: 2.0,
    fontSize: 40, bold: true, color: WHITE, fontFace: "Arial", align: "left",
  });
  s.addText("A simple way for a panchayat office to record every complaint,\nsee what is still waiting, and act on the oldest cases first.", {
    x: 0.8, y: 4.1, w: 10.5, h: 1.0,
    fontSize: 16, color: "E3EDE7", fontFace: "Arial",
  });
  s.addText("SIH 2026 · Internal Practical Assessment", {
    x: 0.8, y: 6.7, w: 8, h: 0.4,
    fontSize: 12, color: "CFE3D8", fontFace: "Arial",
  });
}

// ---------- Slide 2: Problem & who is affected ----------
{
  const s = baseSlide();
  title(s, "The Problem");
  s.addText(
    "Complaints about a blocked drain, a broken street light, or a damaged road are written\n" +
    "in a register or on loose slips at the panchayat office.",
    { x: 0.6, y: 1.5, w: 12.1, h: 1.0, fontSize: 16, color: DARK, fontFace: "Arial" }
  );

  const cards = [
    { h: "Villagers", t: "Return again and again just to ask what happened to their complaint." },
    { h: "The panchayat clerk", t: "Cannot say how many complaints are open or which have waited longest." },
    { h: "The panchayat secretary", t: "Cannot show what was done, or which department is falling behind." },
  ];
  cards.forEach((c, i) => {
    const x = 0.6 + i * 4.15;
    s.addShape("rect", { x, y: 2.9, w: 3.85, h: 2.9, fill: { color: LIGHT_BG }, line: { type: "none" } });
    s.addText(c.h, { x: x + 0.25, y: 3.1, w: 3.35, h: 0.5, fontSize: 16, bold: true, color: GREEN, fontFace: "Arial" });
    s.addText(c.t, { x: x + 0.25, y: 3.65, w: 3.35, h: 1.9, fontSize: 13, color: DARK, fontFace: "Arial" });
  });

  s.addText("The information exists - it is simply not in a form anyone can act on or be held to.", {
    x: 0.6, y: 6.2, w: 12.1, h: 0.7, fontSize: 15, italic: true, color: MUTED, fontFace: "Arial",
  });
}

// ---------- Slide 3: Solution in one sentence ----------
{
  const s = baseSlide();
  title(s, "Our Solution, in One Sentence");
  s.addShape("rect", { x: 0.6, y: 2.1, w: 12.1, h: 2.0, fill: { color: LIGHT_BG }, line: { type: "none" } });
  s.addText(
    "A single online register where every complaint is logged against the right department,\n" +
    "stays visible until it is closed, and is shown to the clerk with the oldest, still-waiting\n" +
    "complaints at the very top.",
    { x: 1.0, y: 2.35, w: 11.3, h: 1.6, fontSize: 20, bold: true, color: DARK, fontFace: "Arial", valign: "middle" }
  );
  s.addText("It also gives the clerk an early flag for complaints that look likely to be delayed,\nso those can be prioritised before they become a repeat complaint.", {
    x: 0.6, y: 4.5, w: 12.1, h: 1.0, fontSize: 15, color: MUTED, fontFace: "Arial",
  });
}

// ---------- Slide 4: Recording a grievance (screenshot) ----------
{
  const s = baseSlide();
  title(s, "Recording a Grievance");
  s.addText("The ward clerk fills a short form. Every field is checked on the server before it is saved.", {
    x: 0.6, y: 1.35, w: 12.1, h: 0.5, fontSize: 14, color: MUTED, fontFace: "Arial",
  });
  placeholder(s, 0.6, 2.0, 12.1, 4.9, "[ Add a screenshot of your running \"Record a New Grievance\" form here ]");
}

// ---------- Slide 5: Register listing (screenshot + calc) ----------
{
  const s = baseSlide();
  title(s, "The Register: Search, Filter, Oldest First");
  placeholder(s, 0.6, 1.4, 12.1, 3.6, "[ Add a screenshot of your running grievance list/search screen here ]");
  s.addShape("rect", { x: 0.6, y: 5.2, w: 12.1, h: 1.7, fill: { color: LIGHT_BG }, line: { type: "none" } });
  s.addText("How the waiting time is calculated", {
    x: 0.9, y: 5.35, w: 11.5, h: 0.4, fontSize: 14, bold: true, color: GREEN, fontFace: "Arial",
  });
  s.addText(
    "Open / In Progress cases: days waiting = today's date − date the grievance was raised.\n" +
    "Resolved cases: days to resolve = the date it was closed − the date it was raised.\n" +
    "The list is sorted so open cases come first, oldest date first - the oldest unresolved complaint is always at the top.",
    { x: 0.9, y: 5.7, w: 11.5, h: 1.1, fontSize: 12.5, color: DARK, fontFace: "Arial" }
  );
}

// ---------- Slide 6: What works / unfinished ----------
{
  const s = baseSlide();
  title(s, "What Works Today, What Is Unfinished");
  s.addShape("rect", { x: 0.6, y: 1.5, w: 5.9, h: 4.9, fill: { color: LIGHT_BG }, line: { type: "none" } });
  s.addText("Working", { x: 0.9, y: 1.7, w: 5.3, h: 0.4, fontSize: 16, bold: true, color: GREEN, fontFace: "Arial" });
  s.addText(
    "• Recording a grievance end to end, with server-side validation\n" +
    "• Searching, filtering and ordering the register by urgency\n" +
    "• A simple risk-of-delay prediction shown at creation time\n" +
    "• Loading, empty and error states on every screen",
    { x: 0.9, y: 2.15, w: 5.3, h: 4.0, fontSize: 13.5, color: DARK, fontFace: "Arial", lineSpacingMultiple: 1.3 }
  );

  s.addShape("rect", { x: 6.8, y: 1.5, w: 5.9, h: 4.9, fill: { color: LIGHT_BG }, line: { type: "none" } });
  s.addText("Not yet finished", { x: 7.1, y: 1.7, w: 5.3, h: 0.4, fontSize: 16, bold: true, color: "8A5B00", fontFace: "Arial" });
  s.addText(
    "• [ State here anything you did not get to - e.g. editing/updating\n   an existing grievance's status from the UI ]\n" +
    "• [ State any screen you left basic on purpose given the 2-day scope ]",
    { x: 7.1, y: 2.15, w: 5.3, h: 4.0, fontSize: 13.5, color: DARK, fontFace: "Arial", lineSpacingMultiple: 1.3 }
  );
}

// ---------- Slide 7: One improvement next ----------
{
  const s = baseSlide();
  title(s, "One Improvement We Would Make Next");
  s.addShape("rect", { x: 0.6, y: 2.4, w: 12.1, h: 2.6, fill: { color: LIGHT_BG }, line: { type: "none" } });
  s.addText(
    "[ Write one concrete next step here - for example: send an SMS to the complainant\n" +
    "automatically when their grievance's status changes, so they never need to call the\n" +
    "office to check. ]",
    { x: 1.0, y: 2.7, w: 11.3, h: 2.0, fontSize: 18, color: DARK, fontFace: "Arial", valign: "middle", italic: true }
  );
}

pres.writeFile({ fileName: "presentation.pptx" }).then(() => console.log("done"));
